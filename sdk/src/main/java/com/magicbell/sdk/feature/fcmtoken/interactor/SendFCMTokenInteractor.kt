package com.magicbell.sdk.feature.fcmtoken.interactor

import com.mobilejazz.harmony.domain.interactor.PutInteractor
import com.magicbell.sdk.common.query.UserQuery
import com.magicbell.sdk.feature.fcmtoken.FCMToken
import com.magicbell.sdk.feature.fcmtoken.data.RegisterFCMTokenQuery
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

internal class SendFCMTokenInteractor(
  private val coroutineContext: CoroutineContext,
  private val putFCMTokenInteractor: PutInteractor<FCMToken>,
) {

  suspend operator fun invoke(deviceToken: String, userQuery: UserQuery): FCMToken {
    return withContext(coroutineContext) {
      val FCMToken = FCMToken(null, deviceToken)
      val registerFCMTokenQuery = RegisterFCMTokenQuery(userQuery)
      putFCMTokenInteractor(FCMToken, registerFCMTokenQuery)
    }
  }
}