package com.magicbell.sdk.feature.store

import com.magicbell.sdk.common.network.graphql.CursorPredicate
import com.magicbell.sdk.common.network.graphql.GraphQLRepresentable

internal class StoreContext(
  val storePredicate: StorePredicate,
  val cursorPredicate: CursorPredicate,
) : GraphQLRepresentable {
  override val graphQLValue: String
    get() {
      val storePredicateString = storePredicate.graphQLValue
      val cursorPredicateString = cursorPredicate.graphQLValue

      return " data: notifications ($storePredicateString, $cursorPredicateString) { ...notification }"
    }
}