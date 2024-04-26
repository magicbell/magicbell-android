package com.magicbell.sdk.feature.notificationpreferences.interactor

import com.mobilejazz.harmony.domain.interactor.PutInteractor
import com.magicbell.sdk.common.query.UserQuery
import com.magicbell.sdk.feature.notificationpreferences.NotificationPreferences
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

internal class UpdateNotificationPreferencesInteractor(
  private val coroutineContext: CoroutineContext,
  private val saveNotificationPreferencesInteractor: PutInteractor<NotificationPreferences>,
) {

  suspend operator fun invoke(notificationPreferences: NotificationPreferences, userQuery: UserQuery): NotificationPreferences {
    return withContext(coroutineContext) {
      saveNotificationPreferencesInteractor(notificationPreferences, userQuery)
    }
  }
}