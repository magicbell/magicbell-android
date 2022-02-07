package com.magicbell.sdk.feature.store.data

import com.mobilejazz.harmony.data.mapper.Mapper
import com.magicbell.sdk.common.network.graphql.GraphQLResponse
import com.magicbell.sdk.feature.store.StorePage
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

internal class GraphQLResponseToStorePageMapper(
  private val serializer: KSerializer<GraphQLResponse<StorePage>>,
  private val json: Json,
) : Mapper<String, Map<String, StorePage>> {
  override fun map(from: String): Map<String, StorePage> {
    val graphQLResponse = json.decodeFromString(serializer, from)
    return graphQLResponse.response
  }
}