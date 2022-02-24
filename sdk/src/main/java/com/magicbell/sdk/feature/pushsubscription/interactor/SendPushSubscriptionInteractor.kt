package com.magicbell.sdk.feature.pushsubscription.interactor

import com.mobilejazz.harmony.domain.interactor.PutInteractor
import com.magicbell.sdk.common.query.UserQuery
import com.magicbell.sdk.feature.pushsubscription.PushSubscription
import com.magicbell.sdk.feature.pushsubscription.data.RegisterPushSubscriptionQuery
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

internal class SendPushSubscriptionInteractor(
  private val coroutineContext: CoroutineContext,
  private val putPushSubscriptionInteractor: PutInteractor<PushSubscription>,
) {

  suspend operator fun invoke(deviceToken: String, userQuery: UserQuery): PushSubscription {
    return withContext(coroutineContext) {
      val pushSubscription = PushSubscription(null, deviceToken, PushSubscription.platformAndroid)
      val registerPushSubscriptionQuery = RegisterPushSubscriptionQuery(userQuery)
      putPushSubscriptionInteractor(pushSubscription, registerPushSubscriptionQuery)
    }
  }
}