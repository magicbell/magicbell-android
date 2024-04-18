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

  /**
   * Fetches the preferences for a given category.
   *
   * @return A Result with the category preferences
   */
  suspend fun fetchPreferences(category: String): Result<Preferences>

  /**
   * Updates the preferences for a given category.
   *
   * @return A Result with the updated category preferences
   */
  suspend fun updatePreferences(category: String, preferences: Preferences): Result<Preferences>
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

  override suspend fun fetchPreferences(category: String): Result<Preferences> {
    return runCatching {
      val userPreferences = getNotificationPreferencesInteractor(userQuery)
      userPreferences.preferences[category] ?: throw MagicBellError("Notification preferences not found for category $category")
    }
  }

  override suspend fun updatePreferences(category: String, preferences: Preferences): Result<Preferences> {
    return runCatching {
      val notificationPreferences = updateNotificationPreferencesInteractor(NotificationPreferences(mapOf(category to preferences)), userQuery)
      notificationPreferences.preferences[category] ?: throw MagicBellError("Notification preferences not found for category $category")
    }
  }
}