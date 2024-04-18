package com.magicbell.sdk

import com.magicbell.sdk.common.query.UserQuery
import com.magicbell.sdk.feature.pushsubscription.PushSubscriptionDirector
import com.magicbell.sdk.feature.store.InternalStoreDirector
import com.magicbell.sdk.feature.store.StoreDirector
import com.magicbell.sdk.feature.notificationpreferences.NotificationPreferencesDirector

class User internal constructor(
  private val userQuery: UserQuery,
  private val storeDirector: InternalStoreDirector,
  val preferences: NotificationPreferencesDirector,
  internal val pushSubscription: PushSubscriptionDirector,
) {

  val store: StoreDirector
    get() {
      return storeDirector
    }

  suspend fun sendDeviceToken(deviceToken: String) {
    pushSubscription.sendPushSubscription(deviceToken)
  }

  suspend fun logout(deviceToken: String?) {
    storeDirector.logout()
    deviceToken?.also {
      pushSubscription.deletePushSubscription(it)
    }
  }
}
