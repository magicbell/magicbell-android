package com.magicbell.sdk.feature.fcmtoken

import com.magicbell.sdk.common.query.UserQuery
import com.magicbell.sdk.feature.fcmtoken.interactor.DeleteFCMTokenInteractor
import com.magicbell.sdk.feature.fcmtoken.interactor.SendFCMTokenInteractor
import kotlinx.coroutines.delay

interface FCMTokenDirector {
  /**
   * Sends a FCM token
   */
  suspend fun sendFCMToken(deviceToken: String)

  /**
   * Deletes a FCM token
   */
  suspend fun deleteFCMToken(deviceToken: String)
}

internal class DefaultFCMTokenDirector(
  private val userQuery: UserQuery,
  private val sendFCMTokenInteractor: SendFCMTokenInteractor,
  private val deleteFCMTokenInteractor: DeleteFCMTokenInteractor,
) : FCMTokenDirector {
  override suspend fun sendFCMToken(deviceToken: String) {
    runCatching {
      sendFCMTokenInteractor(deviceToken, userQuery)
    }.onFailure {
      delay(10000)
      sendFCMToken(deviceToken)
    }
  }

  override suspend fun deleteFCMToken(deviceToken: String) {
    runCatching {
      deleteFCMTokenInteractor(deviceToken, userQuery)
    }.onFailure {
      delay(10000)
      deleteFCMToken(deviceToken)
    }
  }
}