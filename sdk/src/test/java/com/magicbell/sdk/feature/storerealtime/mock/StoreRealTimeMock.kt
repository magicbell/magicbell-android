package com.magicbell.sdk.feature.storerealtime.mock

import com.magicbell.sdk.feature.config.Config
import com.magicbell.sdk.feature.realtime.StoreRealTime
import com.magicbell.sdk.feature.realtime.StoreRealTimeNotificationChange
import com.magicbell.sdk.feature.realtime.StoreRealTimeObserver
import java.util.WeakHashMap

internal sealed class RealTimeEventMock {
  class NewNotification(val id: String) : RealTimeEventMock()
  class DeleteNotification(val id: String) : RealTimeEventMock()
  class ReadNotification(val id: String) : RealTimeEventMock()
  class UnreadNotification(val id: String) : RealTimeEventMock()
  class ArchiveNotification(val id: String) : RealTimeEventMock()
  object ReadAllNotification : RealTimeEventMock()
  object SeenAllNotification : RealTimeEventMock()
  object ReloadStore : RealTimeEventMock()
}

internal class StoreRealTimeMock : StoreRealTime {
  var observers = WeakHashMap<StoreRealTimeObserver, StoreRealTimeObserver>()
  val events: MutableList<MethodParams.ProcessMessage> = mutableListOf()

  override fun startListening(config: Config) {}

  override fun stopListening() {}

  override fun addObserver(observer: StoreRealTimeObserver) {
    observers[observer] = observer
  }

  override fun removeObserver(observer: StoreRealTimeObserver) {
    observers.remove(observer)
  }

  internal fun processMessage(event: RealTimeEventMock) {
    events.add(MethodParams.ProcessMessage(event))
    when (event) {
      is RealTimeEventMock.NewNotification -> forEachObserver { it.notifyNewNotification(event.id) }
      is RealTimeEventMock.DeleteNotification -> forEachObserver { it.notifyDeleteNotification(event.id) }
      is RealTimeEventMock.ReadNotification -> forEachObserver { it.notifyNotificationChange(event.id, StoreRealTimeNotificationChange.READ) }
      is RealTimeEventMock.UnreadNotification -> forEachObserver { it.notifyNotificationChange(event.id, StoreRealTimeNotificationChange.UNREAD) }
      is RealTimeEventMock.ArchiveNotification -> forEachObserver { it.notifyNotificationChange(event.id, StoreRealTimeNotificationChange.ARCHIVED) }

      RealTimeEventMock.ReloadStore -> forEachObserver { it.notifyReloadStore() }
      RealTimeEventMock.ReadAllNotification -> forEachObserver { it.notifyAllNotificationRead() }
      RealTimeEventMock.SeenAllNotification -> forEachObserver { it.notifyAllNotificationSeen() }
    }
  }

  private fun forEachObserver(action: (StoreRealTimeObserver) -> Unit) {
    observers.values.forEach { action(it) }
  }

  sealed class MethodParams {
    class ProcessMessage(event: RealTimeEventMock) : MethodParams()
  }
}