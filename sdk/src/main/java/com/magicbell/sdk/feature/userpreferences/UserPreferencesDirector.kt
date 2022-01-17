package com.magicbell.sdk.feature.userpreferences

import com.magicbell.sdk.common.error.MagicBellError
import com.magicbell.sdk.common.query.UserQuery
import com.magicbell.sdk.feature.userpreferences.interactor.GetUserPreferencesInteractor
import com.magicbell.sdk.feature.userpreferences.interactor.UpdateUserPreferencesInteractor

interface UserPreferencesDirector {
  /**
   * Fetches the user preferences.
   *
   * @return A Result with the user preferences
   */
  suspend fun fetch(): Result<UserPreferences>

  /**
   * Updates the user preferences.
   *
   * @return A Result with the updated user preferences
   */
  suspend fun update(userPreferences: UserPreferences): Result<UserPreferences>

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

internal class DefaultUserPreferencesDirector(
  private val userQuery: UserQuery,
  private val getUserPreferencesInteractor: GetUserPreferencesInteractor,
  private val updateUserPreferencesInteractor: UpdateUserPreferencesInteractor,
) : UserPreferencesDirector {
  override suspend fun fetch(): Result<UserPreferences> {
    return runCatching {
      getUserPreferencesInteractor(userQuery)
    }
  }

  override suspend fun update(userPreferences: UserPreferences): Result<UserPreferences> {
    return runCatching {
      updateUserPreferencesInteractor(userPreferences, userQuery)
    }
  }

  override suspend fun fetchPreferences(category: String): Result<Preferences> {
    return runCatching {
      val userPreferences = getUserPreferencesInteractor(userQuery)
      userPreferences.preferences[category] ?: throw MagicBellError("Notification preferences not found for category $category")
    }
  }

  override suspend fun updatePreferences(category: String, preferences: Preferences): Result<Preferences> {
    return runCatching {
      val userPreferences = updateUserPreferencesInteractor(UserPreferences(mapOf(category to preferences)), userQuery)
      userPreferences.preferences[category] ?: throw MagicBellError("Notification preferences not found for category $category")
    }
  }
}