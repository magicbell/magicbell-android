package com.magicbell.sdk.feature.realtime

import com.magicbell.sdk.common.environment.Environment
import com.magicbell.sdk.common.query.UserQuery
import com.magicbell.sdk.feature.config.Config
import com.magicbell.sdk.feature.realtime.StoreRealTimeNotificationChange.ARCHIVED
import com.magicbell.sdk.feature.realtime.StoreRealTimeNotificationChange.READ
import com.magicbell.sdk.feature.realtime.StoreRealTimeNotificationChange.UNREAD
import io.ably.lib.realtime.AblyRealtime
import io.ably.lib.realtime.ConnectionState
import io.ably.lib.realtime.ConnectionStateListener
import io.ably.lib.types.ClientOptions
import io.ably.lib.types.Message
import io.ably.lib.types.Param
import java.util.WeakHashMap

internal class AblyConnector(
  private val environment: Environment,
  private val userQuery: UserQuery,
) : StoreRealTime {

  private val ablyMessageProcessor = AblyMessageProcessor()

  var status: StoreRealTimeStatus = StoreRealTimeStatus.DISCONNECTED
    private set
  private var ablyClient: AblyRealtime? = null
  private var config: Config? = null

  private var observers: WeakHashMap<StoreRealTimeObserver, StoreRealTimeObserver> = WeakHashMap()

  override fun startListening(config: Config) {
    this.config = config
    if (status == StoreRealTimeStatus.DISCONNECTED) {
      status = StoreRealTimeStatus.CONNECTING
      connect(config)
    }
  }

  override fun stopListening() {
    disconnect()
    status = StoreRealTimeStatus.DISCONNECTED
  }

  override fun addObserver(observer: StoreRealTimeObserver) {
    observers[observer] = observer
  }

  override fun removeObserver(observer: StoreRealTimeObserver) {
    observers.remove(observer)
  }

  private fun connect(config: Config) {
    ablyClient?.connection?.close()

    val clientOptions = ClientOptions()
    clientOptions.authUrl = "${environment.baseUrl}/ws/auth"
    clientOptions.authMethod = "POST"
    val headers = generateAblyHeaders(environment.apiKey,
      userQuery.externalId,
      userQuery.email,
      userQuery.hmac)
    clientOptions.authHeaders = headers
    ablyClient = AblyRealtime(clientOptions)

    startListenConnectionChanges()
    startListenMessages(config.channel)
  }

  private fun disconnect() {
    ablyClient?.connection?.close()
    ablyClient = null
    observers.clear()
  }

  private fun generateAblyHeaders(
    apiKey: String,
    externalId: String?,
    email: String?,
    hmac: String?
  ): Array<Param> {
    val headers = mutableListOf(Param("X-MAGICBELL-API-KEY", apiKey))

    hmac?.also {
      headers.add(Param("X-MAGICBELL-USER-HMAC", it))
    }

    externalId?.also {
      headers.add(Param("X-MAGICBELL-USER-EXTERNAL-ID", it))
    }
    email?.also {
      headers.add(Param("X-MAGICBELL-USER-EMAIL", it))
    }

    return headers.toTypedArray()
  }

  private fun startListenConnectionChanges() {
    ablyClient?.connection?.on(ConnectionStateListener { stateChange ->
      when (stateChange.current) {
        ConnectionState.connected -> status = StoreRealTimeStatus.CONNECTED
        ConnectionState.disconnected -> status = StoreRealTimeStatus.CONNECTING
        ConnectionState.suspended -> status = StoreRealTimeStatus.CONNECTING
        ConnectionState.closed -> {
          if (status != StoreRealTimeStatus.DISCONNECTED && config != null) {
            forEachObserver { it.notifyReloadStore() }
            status = StoreRealTimeStatus.CONNECTING
            connect(config!!)
          }
        }
        ConnectionState.initialized,
        ConnectionState.connecting,
        ConnectionState.closing,
        ConnectionState.failed,
        -> {
          // Do nothing
        }
      }
    })
  }

  private fun startListenMessages(channel: String) {
    ablyClient?.channels?.get(channel)?.subscribe { message ->
      processAblyMessage(message)
    }
  }

  private fun processAblyMessage(message: Message) {
    return when (val realTimeMessage = ablyMessageProcessor.processMessage(message)) {
      is AblyMessageProcessor.RealTimeMessage.New -> forEachObserver { it.notifyNewNotification(realTimeMessage.notificationId) }
      is AblyMessageProcessor.RealTimeMessage.Read -> forEachObserver { it.notifyNotificationChange(realTimeMessage.notificationId, READ) }
      is AblyMessageProcessor.RealTimeMessage.Unread -> forEachObserver { it.notifyNotificationChange(realTimeMessage.notificationId, UNREAD) }
      is AblyMessageProcessor.RealTimeMessage.Delete -> forEachObserver { it.notifyDeleteNotification(realTimeMessage.notificationId) }
      is AblyMessageProcessor.RealTimeMessage.Archived -> forEachObserver { it.notifyNotificationChange(realTimeMessage.notificationId, ARCHIVED) }
      AblyMessageProcessor.RealTimeMessage.ReadAll -> forEachObserver { it.notifyAllNotificationRead() }
      AblyMessageProcessor.RealTimeMessage.SeenAll -> forEachObserver { it.notifyAllNotificationSeen() }
    }
  }

  private fun forEachObserver(action: (StoreRealTimeObserver) -> Unit) {
    observers.values.forEach { action(it) }
  }
}
