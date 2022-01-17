package com.magicbell.sdk.feature.config.interactor

import com.harmony.kotlin.data.operation.CacheSyncOperation
import com.harmony.kotlin.data.operation.MainSyncOperation
import com.harmony.kotlin.domain.interactor.GetInteractor
import com.magicbell.sdk.common.query.UserQuery
import com.magicbell.sdk.feature.config.Config
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

// TODO: 14/1/22 Internal
class GetConfigInteractor(
  private val coroutineContext: CoroutineContext,
  private val getConfigInteractor: GetInteractor<Config>,
) {

  suspend operator fun invoke(forceRefresh: Boolean, userQuery: UserQuery): Config {
    return withContext(coroutineContext) {
      val operation = if (forceRefresh) MainSyncOperation else CacheSyncOperation()
      getConfigInteractor(userQuery, operation)
    }
  }
}