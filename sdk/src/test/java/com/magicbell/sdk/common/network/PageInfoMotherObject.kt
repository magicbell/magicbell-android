package com.magicbell.sdk.common.network

import com.harmony.kotlin.common.randomBoolean
import com.magicbell.sdk.common.network.graphql.PageInfo

internal fun anyPageInfo(): PageInfo {
  return PageInfo(
    endCursor = AnyCursor.ANY.value,
    hasNextPage = randomBoolean(),
    hasPreviousPage = randomBoolean(),
    startCursor = AnyCursor.ANY.value
  )
}

internal class PageInfoMotherObject {
  companion object {
    fun createPageInfo(
      endCursor: String? = null,
      hasNextPage: Boolean = randomBoolean(),
      hasPreviousPage: Boolean = randomBoolean(),
      startCursor: String? = null
    ): PageInfo {
      return PageInfo(endCursor, hasNextPage, hasPreviousPage, startCursor)
    }
  }
}