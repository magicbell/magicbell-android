package com.magicbell.sdk

import com.magicbell.sdk.common.query.UserQuery
import com.magicbell.sdk.feature.fcmtoken.FCMTokenDirector
import com.magicbell.sdk.feature.store.InternalStoreDirector
import com.magicbell.sdk.feature.store.StoreDirector
import com.magicbell.sdk.feature.notificationpreferences.NotificationPreferencesDirector

class User internal constructor(
  private val userQuery: UserQuery,
  private val storeDirector: InternalStoreDirector,
  val preferences: NotificationPreferencesDirector,
  internal val fcmToken: FCMTokenDirector,
) {

  val store: StoreDirector
    get() {
      return storeDirector
    }

  suspend fun sendDeviceToken(deviceToken: String) {
    fcmToken.sendFCMToken(deviceToken)
  }

  suspend fun logout(deviceToken: String?) {
    storeDirector.logout()
    deviceToken?.also {
      fcmToken.deleteFCMToken(it)
    }
  }
}
