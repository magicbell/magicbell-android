package com.magicbell.sdk.feature.store.data

import com.magicbell.sdk.common.network.QueryParameters
import com.magicbell.sdk.feature.store.StoreContext
import com.magicbell.sdk.feature.store.StorePagePredicate
import com.magicbell.sdk.feature.store.StorePredicate

internal fun StoreContext.asQueryParameters(): QueryParameters {
  return storePredicate.asQueryParameters() + storePagePredicate.asQueryParameters()
}

private fun StorePredicate.asQueryParameters(): QueryParameters {
  val result: MutableList<Pair<String, String>> = mutableListOf()

  read?.let { result += "read" to "$it" }
  seen?.let { result += "seen" to "$it" }
  result += "archived" to "$archived"

  category?.let { result += "category" to it }

  topic?.let { result += "topic" to it }

  return result
}

private fun StorePagePredicate.asQueryParameters(): QueryParameters {
  return listOf(
    "page" to "$page",
    "per_page" to "$size",
    )
}

