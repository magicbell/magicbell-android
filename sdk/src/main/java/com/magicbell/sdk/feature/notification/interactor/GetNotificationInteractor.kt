package com.magicbell.sdk.feature.notification.interactor

import com.harmony.kotlin.domain.interactor.GetInteractor
import com.magicbell.sdk.common.query.UserQuery
import com.magicbell.sdk.feature.notification.Notification
import com.magicbell.sdk.feature.notification.data.NotificationQuery
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

internal interface GetNotificationInteractor {
  suspend operator fun invoke(notificationId: String, userQuery: UserQuery): Notification
}

internal class GetNotificationDefaultInteractor(
  private val coroutineContext: CoroutineContext,
  private val getNotificationInteractor: GetInteractor<Notification>,
) : GetNotificationInteractor {

  override suspend operator fun invoke(notificationId: String, userQuery: UserQuery): Notification {
    return withContext(coroutineContext) {
      getNotificationInteractor(NotificationQuery(notificationId, userQuery))
    }
  }
}