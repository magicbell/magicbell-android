package com.magicbell.sdk.feature.userpreferences

import com.magicbell.sdk.common.error.MagicBellError
import com.magicbell.sdk.common.query.UserQuery
import com.magicbell.sdk.feature.userpreferences.interactor.GetUserPreferencesInteractor
import com.magicbell.sdk.feature.userpreferences.interactor.UpdateUserPreferencesInteractor

interface UserPreferencesDirector {
  suspend fun fetch(): Result<UserPreferences>

  suspend fun update(userPreferences: UserPreferences): Result<UserPreferences>

  suspend fun fetchPreferences(category: String): Result<Preferences>

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