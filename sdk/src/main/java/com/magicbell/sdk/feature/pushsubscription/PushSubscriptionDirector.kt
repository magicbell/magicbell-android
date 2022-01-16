package com.magicbell.sdk.feature.pushsubscription

import com.magicbell.sdk.common.query.UserQuery
import com.magicbell.sdk.feature.pushsubscription.interactor.DeletePushSubscriptionInteractor
import com.magicbell.sdk.feature.pushsubscription.interactor.SendPushSubscriptionInteractor
import kotlinx.coroutines.delay

interface PushSubscriptionDirector {
  suspend fun sendPushSubscription(deviceToken: String)

  suspend fun deletePushSubscription(deviceToken: String)
}

internal class DefaultPushSubscriptionDirector(
  private val userQuery: UserQuery,
  private val sendPushSubscriptionInteractor: SendPushSubscriptionInteractor,
  private val deletePushSubscriptionInteractor: DeletePushSubscriptionInteractor,
) : PushSubscriptionDirector {
  override suspend fun sendPushSubscription(deviceToken: String) {
    runCatching {
      sendPushSubscriptionInteractor(deviceToken, userQuery)
    }.onFailure {
      delay(10000)
      sendPushSubscription(deviceToken)
    }
  }

  override suspend fun deletePushSubscription(deviceToken: String) {
    runCatching {
      deletePushSubscriptionInteractor(deviceToken, userQuery)
    }.onFailure {
      delay(10000)
      deletePushSubscription(deviceToken)
    }
  }
}