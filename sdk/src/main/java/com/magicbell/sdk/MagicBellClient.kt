package com.magicbell.sdk

import android.content.Context
import com.magicbell.sdk.common.environment.Environment
import com.magicbell.sdk.common.logger.LogLevel
import com.magicbell.sdk.common.query.UserQuery
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.URL

/**
 * Public MagicBell SDK interface
 *
 * @param apiKey The API Key of your account.
 * @param apiSecret The API Secret of your account.
 * @param enableHMAC Enables HMAC authentication. Default to false. If set to true, HMAC will be only enabled if api secret is provided.
 * @param baseURL The base url of the api server. Default to `MagicBell.defaultBaseUrl`.
 * @param logLevel The log level accepts none or debug. Default to none.
 * @param context The application context
 */
class MagicBellClient(
  private val apiKey: String,
  private val apiSecret: String? = null,
  private val enableHMAC: Boolean = false,
  private val baseURL: URL = defaultBaseUrl,
  private val logLevel: LogLevel = LogLevel.NONE,
  private val magicBellScope: CoroutineScope = CoroutineScope(Dispatchers.Main),
  private val context: Context,
) {

  companion object {
    val defaultBaseUrl = URL("https://api.magicbell.com")
  }

  private val sdkComponent: SDKComponent

  private var users: MutableMap<String, User> = mutableMapOf()
  private var deviceToken: String? = null

  init {
    sdkComponent = DefaultSDKModule(
      Environment(apiKey, apiSecret, baseURL, enableHMAC),
      logLevel,
      context,
      magicBellScope
    )
  }

  /**
   * Creates or retrieve an existing user.
   *
   * @param email The user's email.
   * @return A instance of User.
   */
  fun connectUserEmail(email: String): User {
    val userQuery = UserQuery.createEmail(email)
    return getUser(userQuery)
  }

  /**
   * Creates or retrieve an existing user.
   *
   * @param externalId The user's external id.
   * @return A instance of User.
   */
  fun connectUserExternalId(externalId: String): User {
    val userQuery = UserQuery.createExternalId(externalId)
    return getUser(userQuery)
  }

  /**
   * Creates or retrieve an existing user.
   *
   * @param externalId The user's external id.
   * @param email The user's email.
   * @return A instance of User.
   */
  fun connectUserWith(email: String, externalId: String): User {
    val userQuery = UserQuery.create(externalId, email)
    return getUser(userQuery)
  }

  /**
   * Removes an existing user and stops all the connections.
   *
   * @param email The user's email.
   */
  fun disconnectUserEmail(email: String) {
    val userQuery = UserQuery.createEmail(email)
    return removeUser(userQuery)
  }

  /**
   * Removes an existing user and stops all the connections.
   *
   * @param externalId The user's external id.
   */
  fun disconnectUserExternalId(externalId: String) {
    val userQuery = UserQuery.createExternalId(externalId)
    return removeUser(userQuery)
  }

  /**
   * Removes an existing user and stops all the connections.
   *
   * @param externalId The user's email.
   * @param email The user's email.
   */
  fun disconnectUserWith(email: String, externalId: String) {
    val userQuery = UserQuery.create(externalId, email)
    return removeUser(userQuery)
  }

  /**
   * Sets the APN token for the current logged user. This token is revoked when logout is called.
   *
   * @param deviceToken FCM Token.
   */
  fun setDeviceToken(deviceToken: String) {
    this.deviceToken = deviceToken
    users.values.forEach { user ->
      magicBellScope.launch {
        user.pushSubscription.sendPushSubscription(deviceToken)
      }
    }
  }

  private fun getUser(userQuery: UserQuery): User {
    users[userQuery.key]?.let { return it }

    val user = User(
      userQuery,
      sdkComponent.storeComponent().storeDirector(userQuery),
      sdkComponent.userPreferencesComponent().userPreferencesDirector(userQuery),
      sdkComponent.pushSubscriptionComponent().getPushSubscriptionDirector(userQuery)
    )

    users[userQuery.key] = user
    deviceToken?.also { deviceToken ->
      magicBellScope.launch {
        user.pushSubscription.sendPushSubscription(deviceToken)
      }
    }

    return user
  }

  private fun removeUser(userQuery: UserQuery) {
    users[userQuery.key]?.also { user ->
      magicBellScope.launch {
        user.logout(deviceToken)
        users.remove(userQuery.key)
      }
    }
  }
}
