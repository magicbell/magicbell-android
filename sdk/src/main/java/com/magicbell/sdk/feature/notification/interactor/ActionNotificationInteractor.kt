package com.magicbell.sdk.feature.notification.interactor

import com.harmony.kotlin.domain.interactor.PutInteractor
import com.magicbell.sdk.common.query.UserQuery
import com.magicbell.sdk.feature.notification.data.NotificationActionQuery
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

class ActionNotificationInteractor(
  private val coroutineContext: CoroutineContext,
  private val actionInteractor: PutInteractor<Unit>,
) {

  suspend operator fun invoke(action: NotificationActionQuery.Action, notificationId: String? = null, userQuery: UserQuery) {
    return withContext(coroutineContext) {
      val query = NotificationActionQuery(action, notificationId ?: "", userQuery)
      actionInteractor.invoke(null, query)
    }
  }
}