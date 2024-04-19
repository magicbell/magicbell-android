package com.magicbell.sdk.feature.notificationpreferences

import com.magicbell.sdk.common.error.MagicBellError
import com.magicbell.sdk.common.query.UserQuery
import com.magicbell.sdk.feature.notificationpreferences.interactor.GetNotificationPreferencesInteractor
import com.magicbell.sdk.feature.notificationpreferences.interactor.UpdateNotificationPreferencesInteractor

interface NotificationPreferencesDirector {
  /**
   * Fetches the notification preferences.
   *
   * @return A Result with the notification preferences
   */
  suspend fun fetch(): Result<NotificationPreferences>

  /**
   * Updates the notification preferences.
   *
   * @return A Result with the updated notification preferences
   */
  suspend fun update(notificationPreferences: NotificationPreferences): Result<NotificationPreferences>
}

internal class DefaultNotificationPreferencesDirector(
  private val userQuery: UserQuery,
  private val getNotificationPreferencesInteractor: GetNotificationPreferencesInteractor,
  private val updateNotificationPreferencesInteractor: UpdateNotificationPreferencesInteractor,
) : NotificationPreferencesDirector {
  override suspend fun fetch(): Result<NotificationPreferences> {
    return runCatching {
      getNotificationPreferencesInteractor(userQuery)
    }
  }

  override suspend fun update(notificationPreferences: NotificationPreferences): Result<NotificationPreferences> {
    return runCatching {
      updateNotificationPreferencesInteractor(notificationPreferences, userQuery)
    }
  }
}