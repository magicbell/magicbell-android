package com.magicbell.sdk.common.network.graphql

import kotlinx.serialization.Serializable

@Serializable
internal class PageInfo(
  val endCursor: String?,
  val hasNextPage: Boolean,
  val hasPreviousPage: Boolean,
  val startCursor: String?,
)