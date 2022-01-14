package com.magicbell.sdk.feature.userpreferences.interactor

import com.harmony.kotlin.domain.interactor.PutInteractor
import com.magicbell.sdk.common.query.UserQuery
import com.magicbell.sdk.feature.userpreferences.UserPreferences
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

class UpdateUserPreferencesInteractor(
  private val coroutineContext: CoroutineContext,
  private val saveUserPreferencesInteractor: PutInteractor<UserPreferences>,
) {

  suspend operator fun invoke(userPreferences: UserPreferences, userQuery: UserQuery): UserPreferences {
    return withContext(coroutineContext) {
      saveUserPreferencesInteractor(userPreferences, userQuery)
    }
  }
}