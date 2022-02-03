package com.magicbell.sdk.common.network.graphql

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal class GraphQLResponse<T>(
  @SerialName("data")
  val response: Map<String, T>,
)