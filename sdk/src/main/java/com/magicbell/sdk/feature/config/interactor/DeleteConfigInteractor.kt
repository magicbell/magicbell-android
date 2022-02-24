package com.magicbell.sdk.feature.config.interactor

import com.mobilejazz.harmony.data.operation.CacheOperation
import com.mobilejazz.harmony.domain.interactor.DeleteInteractor
import com.magicbell.sdk.common.query.UserQuery
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

internal interface DeleteConfigInteractor {
  suspend operator fun invoke(userQuery: UserQuery)
}

internal class DeleteConfigDefaultInteractor(
  private val coroutineContext: CoroutineContext,
  private val getConfigInteractor: DeleteInteractor,
) : DeleteConfigInteractor {

  override suspend operator fun invoke(userQuery: UserQuery) {
    return withContext(coroutineContext) {
      getConfigInteractor(userQuery, CacheOperation())
    }
  }
}