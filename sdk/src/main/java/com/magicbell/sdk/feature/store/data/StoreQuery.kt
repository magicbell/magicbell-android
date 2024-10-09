package com.magicbell.sdk.feature.store.data

import com.mobilejazz.harmony.data.query.Query
import com.magicbell.sdk.common.query.UserQuery
import com.magicbell.sdk.feature.store.StoreContext
import com.magicbell.sdk.feature.store.StorePagePredicate
import com.magicbell.sdk.feature.store.StorePredicate

internal class StoreQuery(
  val context: StoreContext,
  val userQuery: UserQuery,
) : Query() {

  constructor(
    storePredicate: StorePredicate,
    storePagePredicate: StorePagePredicate,
    userQuery: UserQuery,
  ) : this(StoreContext(storePredicate, storePagePredicate), userQuery)
}