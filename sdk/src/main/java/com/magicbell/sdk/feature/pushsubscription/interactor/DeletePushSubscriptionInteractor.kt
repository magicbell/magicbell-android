package com.magicbell.sdk.feature.pushsubscription.interactor

import com.mobilejazz.harmony.domain.interactor.DeleteInteractor
import com.magicbell.sdk.common.query.UserQuery
import com.magicbell.sdk.feature.pushsubscription.data.DeletePushSubscriptionQuery
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

internal class DeletePushSubscriptionInteractor(
  private val coroutineContext: CoroutineContext,
  private val deletePushSubscriptionInteractor: DeleteInteractor,
) {

  suspend operator fun invoke(deviceToken: String, userQuery: UserQuery) {
    return withContext(coroutineContext) {
      deletePushSubscriptionInteractor(DeletePushSubscriptionQuery(deviceToken, userQuery))
    }
  }
}