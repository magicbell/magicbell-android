package com.magicbell.sdk.feature.store.interactor

import com.magicbell.sdk.common.network.graphql.CursorPredicate
import com.magicbell.sdk.common.query.UserQuery
import com.magicbell.sdk.feature.store.StorePage
import com.magicbell.sdk.feature.store.StorePredicate
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

internal class FetchStorePageInteractor(
  private val coroutineContext: CoroutineContext,
  private val getStorePagesInteractor: GetStorePagesInteractor,
) {

  suspend operator fun invoke(
    storePredicate: StorePredicate,
    cursorPredicate: CursorPredicate,
    userQuery: UserQuery,
  ): StorePage {
    return withContext(coroutineContext) {
      getStorePagesInteractor(storePredicate, cursorPredicate, userQuery)
    }
  }
}