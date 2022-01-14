package com.magicbell.sdk.feature.userpreferences

import com.magicbell.sdk.common.error.MagicBellError
import com.magicbell.sdk.common.query.UserQuery
import com.magicbell.sdk.feature.userpreferences.interactor.GetUserPreferencesInteractor
import com.magicbell.sdk.feature.userpreferences.interactor.UpdateUserPreferencesInteractor

interface UserPreferencesDirector {
  suspend fun fetch(): UserPreferences

  suspend fun update(userPreferences: UserPreferences): UserPreferences

  suspend fun fetchPreferences(category: String): Preferences

  suspend fun updatePreferences(category: String, preferences: Preferences): Preferences
}

internal class DefaultUserPreferencesDirector(
  private val userQuery: UserQuery,
  private val getUserPreferencesInteractor: GetUserPreferencesInteractor,
  private val updateUserPreferencesInteractor: UpdateUserPreferencesInteractor,
) : UserPreferencesDirector {
  override suspend fun fetch(): UserPreferences {
    return getUserPreferencesInteractor(userQuery)
  }

  override suspend fun update(userPreferences: UserPreferences): UserPreferences {
    return updateUserPreferencesInteractor(userPreferences, userQuery)
  }

  override suspend fun fetchPreferences(category: String): Preferences {
    val userPreferences = getUserPreferencesInteractor(userQuery)
    return userPreferences.preferences[category] ?: throw MagicBellError("Notification preferences not found for category $category")
  }

  override suspend fun updatePreferences(category: String, preferences: Preferences): Preferences {
    val userPreferences = updateUserPreferencesInteractor(UserPreferences(mapOf(category to preferences)), userQuery)
    return userPreferences.preferences[category] ?: throw MagicBellError("Notification preferences not found for category $category")
  }
}