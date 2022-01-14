package com.magicbell.sdk.feature.notification.interactor

import com.harmony.kotlin.domain.interactor.DeleteInteractor
import com.magicbell.sdk.common.query.UserQuery
import com.magicbell.sdk.feature.notification.data.NotificationQuery
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

// TODO: do it internal when notificationstore methods
class DeleteNotificationInteractor(
  private val coroutineContext: CoroutineContext,
  private val deleteNotificationInteractor: DeleteInteractor,
) {

  suspend operator fun invoke(notificationId: String, userQuery: UserQuery) {
    return withContext(coroutineContext) {
      val query = NotificationQuery(notificationId, userQuery)
      deleteNotificationInteractor.invoke(query)
    }
  }
}