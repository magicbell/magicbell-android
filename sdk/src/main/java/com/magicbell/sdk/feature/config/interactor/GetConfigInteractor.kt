package com.magicbell.sdk.feature.config.interactor

import com.mobilejazz.harmony.data.operation.CacheSyncOperation
import com.mobilejazz.harmony.data.operation.MainSyncOperation
import com.mobilejazz.harmony.domain.interactor.GetInteractor
import com.magicbell.sdk.common.query.UserQuery
import com.magicbell.sdk.feature.config.Config
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

internal interface GetConfigInteractor {
  suspend operator fun invoke(forceRefresh: Boolean, userQuery: UserQuery): Config
}

internal class GetConfigDefaultInteractor(
  private val coroutineContext: CoroutineContext,
  private val getConfigInteractor: GetInteractor<Config>,
) : GetConfigInteractor {

  override suspend operator fun invoke(forceRefresh: Boolean, userQuery: UserQuery): Config {
    return withContext(coroutineContext) {
      val operation = if (forceRefresh) MainSyncOperation else CacheSyncOperation()
      getConfigInteractor(userQuery, operation)
    }
  }
}