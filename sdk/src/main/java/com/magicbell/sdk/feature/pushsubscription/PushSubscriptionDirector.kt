package com.magicbell.sdk.feature.pushsubscription

import com.magicbell.sdk.common.query.UserQuery
import com.magicbell.sdk.feature.pushsubscription.interactor.DeletePushSubscriptionInteractor
import com.magicbell.sdk.feature.pushsubscription.interactor.SendPushSubscriptionInteractor

interface PushSubscriptionDirector {
  fun sendPushSubscription(deviceToken: String)

  fun deletePushSubscription(deviceToken: String)
}

internal class DefaultPushSubscriptionDirector(
  private val userQuery: UserQuery,
  private val sendPushSubscriptionInteractor: SendPushSubscriptionInteractor,
  private val deletePushSubscriptionInteractor: DeletePushSubscriptionInteractor,
) : PushSubscriptionDirector {
  override fun sendPushSubscription(deviceToken: String) {
//    sendPushSubscriptionInteractor(deviceToken, userQuery)
  }

  override fun deletePushSubscription(deviceToken: String) {
//    deletePushSubscriptionInteractor(deviceToken, userQuery)
  }
}