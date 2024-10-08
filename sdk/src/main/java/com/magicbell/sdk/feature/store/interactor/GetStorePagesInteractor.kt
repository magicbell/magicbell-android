package com.magicbell.sdk.feature.store.interactor

import com.magicbell.sdk.common.query.UserQuery
import com.magicbell.sdk.feature.store.StoreContext
import com.magicbell.sdk.feature.store.StorePage
import com.magicbell.sdk.feature.store.StorePagePredicate
import com.magicbell.sdk.feature.store.StorePredicate
import com.magicbell.sdk.feature.store.data.StoreQuery
import com.mobilejazz.harmony.domain.interactor.GetInteractor
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

internal class GetStorePagesInteractor(
  private val coroutineContext: CoroutineContext,
  private val getStoreNotificationInteractor: GetInteractor<StorePage>,
) {

  suspend operator fun invoke(
    storePredicate: StorePredicate,
    storePagePredicate: StorePagePredicate,
    userQuery: UserQuery,
  ): StorePage {
    val context = StoreContext(storePredicate, storePagePredicate)
    return withContext(coroutineContext) {
      getStoreNotificationInteractor(StoreQuery(context, userQuery))
    }
  }
}
