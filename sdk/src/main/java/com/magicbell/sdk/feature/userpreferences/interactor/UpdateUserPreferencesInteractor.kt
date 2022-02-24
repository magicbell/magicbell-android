package com.magicbell.sdk.feature.userpreferences.interactor

import com.mobilejazz.harmony.domain.interactor.PutInteractor
import com.magicbell.sdk.common.query.UserQuery
import com.magicbell.sdk.feature.userpreferences.UserPreferences
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

internal class UpdateUserPreferencesInteractor(
  private val coroutineContext: CoroutineContext,
  private val saveUserPreferencesInteractor: PutInteractor<UserPreferences>,
) {

  suspend operator fun invoke(userPreferences: UserPreferences, userQuery: UserQuery): UserPreferences {
    return withContext(coroutineContext) {
      saveUserPreferencesInteractor(userPreferences, userQuery)
    }
  }
}