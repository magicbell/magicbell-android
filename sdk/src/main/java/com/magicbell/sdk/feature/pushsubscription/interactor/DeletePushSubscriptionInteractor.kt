package com.magicbell.sdk.feature.pushsubscription.interactor

import com.harmony.kotlin.domain.interactor.DeleteInteractor
import com.magicbell.sdk.common.query.UserQuery
import com.magicbell.sdk.feature.pushsubscription.data.DeletePushSubscriptionQuery
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

class DeletePushSubscriptionInteractor(
  private val coroutineContext: CoroutineContext,
  private val deletePushSubscriptionInteractor: DeleteInteractor,
) {

  suspend operator fun invoke(deviceToken: String, userQuery: UserQuery) {
    return withContext(coroutineContext) {
      deletePushSubscriptionInteractor(DeletePushSubscriptionQuery(deviceToken, userQuery))
    }
  }
}