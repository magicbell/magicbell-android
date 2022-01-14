package com.magicbell.sdk

import android.content.Context
import com.magicbell.sdk.common.environment.Environment
import com.magicbell.sdk.common.logger.LogLevel
import com.magicbell.sdk.common.query.UserQuery
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.URL

class MagicBell(
  apiKey: String,
  apiSecret: String? = null,
  enableHMAC: Boolean = false,
  baseURL: URL = defaultBaseUrl,
  logLevel: LogLevel = LogLevel.NONE,
  context: Context,
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
      context
    )
  }

  fun forUserEmail(email: String): User {
    val userQuery = UserQuery.createEmail(email)
    return getUser(userQuery)
  }

  fun forUserExternalId(externalId: String): User {
    val userQuery = UserQuery.createExternalId(externalId)
    return getUser(userQuery)
  }

  fun forUser(externalId: String, email: String): User {
    val userQuery = UserQuery.create(externalId, email)
    return getUser(userQuery)
  }

  fun removeUserForEmail(email: String) {
    val userQuery = UserQuery.createEmail(email)
    return removeUser(userQuery)
  }

  fun removeUserForExternalId(externalId: String) {
    val userQuery = UserQuery.createExternalId(externalId)
    return removeUser(userQuery)
  }

  fun removeUserFor(externalId: String, email: String) {
    val userQuery = UserQuery.create(externalId, email)
    return removeUser(userQuery)
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
      CoroutineScope(Dispatchers.IO).launch {
        user.pushSubscription.sendPushSubscription(deviceToken)
      }
    }

    return user
  }

  private fun removeUser(userQuery: UserQuery) {
    users[userQuery.key]?.also { user ->
      CoroutineScope(Dispatchers.IO).launch {
        user.logout(deviceToken)
        users.remove(userQuery.key)
      }
    }
  }

  fun setDeviceToken(deviceToken: String) {
    this.deviceToken = deviceToken
    users.values.forEach { user ->
      CoroutineScope(Dispatchers.IO).launch {
        user.pushSubscription.sendPushSubscription(deviceToken)
      }
    }
  }
}
