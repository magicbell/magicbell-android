package com.magicbell.sdk.feature.store.interactor

import com.magicbell.sdk.common.network.graphql.CursorPredicate
import com.magicbell.sdk.common.query.UserQuery
import com.magicbell.sdk.feature.store.StorePage
import com.magicbell.sdk.feature.store.StorePredicate
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

internal interface FetchStorePageInteractor {
  suspend operator fun invoke(
    storePredicate: StorePredicate,
    cursorPredicate: CursorPredicate,
    userQuery: UserQuery,
  ): StorePage
}

internal class FetchStorePageDefaultInteractor(
  private val interactorCoroutineContext: CoroutineContext,
  private val getStorePagesInteractor: GetStorePagesInteractor,
) : FetchStorePageInteractor {

  override suspend operator fun invoke(
    storePredicate: StorePredicate,
    cursorPredicate: CursorPredicate,
    userQuery: UserQuery,
  ): StorePage {
    return withContext(interactorCoroutineContext) {
      getStorePagesInteractor(storePredicate, cursorPredicate, userQuery)
    }
  }
}