package com.magicbell.sdk.feature.notificationpreferences.interactor

import com.mobilejazz.harmony.domain.interactor.GetInteractor
import com.magicbell.sdk.common.query.UserQuery
import com.magicbell.sdk.feature.notificationpreferences.NotificationPreferences
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

internal class GetNotificationPreferencesInteractor(
  private val coroutineContext: CoroutineContext,
  private val getNotificationPreferencesInteractor: GetInteractor<NotificationPreferences>,
) {

  suspend operator fun invoke(userQuery: UserQuery): NotificationPreferences {
    return withContext(coroutineContext) {
      getNotificationPreferencesInteractor(userQuery)
    }
  }
}