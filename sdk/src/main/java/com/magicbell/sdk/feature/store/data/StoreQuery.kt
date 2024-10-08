package com.magicbell.sdk.feature.store.data

import com.mobilejazz.harmony.data.query.Query
import com.magicbell.sdk.common.network.graphql.CursorPredicate
import com.magicbell.sdk.common.network.graphql.GraphQLRepresentable
import com.magicbell.sdk.common.query.UserQuery
import com.magicbell.sdk.feature.store.StoreContext
import com.magicbell.sdk.feature.store.StorePredicate

internal class StoreQuery(
  val context: StoreContext,
  val userQuery: UserQuery,
) : Query(), GraphQLRepresentable {

  constructor(
    storePredicate: StorePredicate,
    cursorPredicate: CursorPredicate,
    userQuery: UserQuery,
  ) : this(StoreContext(storePredicate, cursorPredicate), userQuery)

  override val graphQLValue: String
    get() {
      return context.graphQLValue
    }
}