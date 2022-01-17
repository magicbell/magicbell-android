package com.magicbell.sdk.feature.notification.interactor

import com.harmony.kotlin.domain.interactor.GetInteractor
import com.magicbell.sdk.common.query.UserQuery
import com.magicbell.sdk.feature.notification.Notification
import com.magicbell.sdk.feature.notification.data.NotificationQuery
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

// TODO: do it internal when notificationstore methods
class GetNotificationInteractor(
  private val coroutineContext: CoroutineContext,
  private val getNotificationInteractor: GetInteractor<Notification>,
) {

  suspend operator fun invoke(notificationId: String, userQuery: UserQuery): Notification {
    return withContext(coroutineContext) {
      getNotificationInteractor(NotificationQuery(notificationId, userQuery))
    }
  }
}