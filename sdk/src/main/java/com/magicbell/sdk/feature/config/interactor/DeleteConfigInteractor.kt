package com.magicbell.sdk.feature.config.interactor

import com.harmony.kotlin.data.operation.CacheOperation
import com.harmony.kotlin.domain.interactor.DeleteInteractor
import com.magicbell.sdk.common.query.UserQuery
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

internal class DeleteConfigInteractor(
  private val coroutineContext: CoroutineContext,
  private val getConfigInteractor: DeleteInteractor,
) {

  suspend operator fun invoke(userQuery: UserQuery) {
    return withContext(coroutineContext) {
      getConfigInteractor(userQuery, CacheOperation())
    }
  }
}