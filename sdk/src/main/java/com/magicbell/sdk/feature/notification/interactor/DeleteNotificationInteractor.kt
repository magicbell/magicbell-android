package com.magicbell.sdk.feature.notification.interactor

import com.mobilejazz.harmony.domain.interactor.DeleteInteractor
import com.magicbell.sdk.common.query.UserQuery
import com.magicbell.sdk.feature.notification.data.NotificationQuery
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

internal interface DeleteNotificationInteractor {
  suspend operator fun invoke(notificationId: String, userQuery: UserQuery)
}

internal class DeleteNotificationDefaultInteractor(
  private val coroutineContext: CoroutineContext,
  private val deleteNotificationInteractor: DeleteInteractor,
) : DeleteNotificationInteractor {

  override suspend operator fun invoke(notificationId: String, userQuery: UserQuery) {
    return withContext(coroutineContext) {
      val query = NotificationQuery(notificationId, userQuery)
      deleteNotificationInteractor.invoke(query)
    }
  }
}