package com.magicbell.sdk.feature.pushsubscription

import com.magicbell.sdk.common.query.UserQuery
import com.magicbell.sdk.feature.pushsubscription.interactor.DeletePushSubscriptionInteractor
import com.magicbell.sdk.feature.pushsubscription.interactor.SendPushSubscriptionInteractor

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
    sendPushSubscriptionInteractor(deviceToken, userQuery)
  }

  override suspend fun deletePushSubscription(deviceToken: String) {
    deletePushSubscriptionInteractor(deviceToken, userQuery)
  }
}