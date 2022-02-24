package com.magicbell.sdk.feature.store.data

import com.mobilejazz.harmony.data.mapper.Mapper
import com.magicbell.sdk.common.network.graphql.GraphQLRequest
import com.magicbell.sdk.common.network.graphql.GraphQLRequestEntity
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

internal class GraphQLRequestToGraphQLEntityMapper(
  val serializer: KSerializer<GraphQLRequestEntity>,
  val json: Json,
) : Mapper<GraphQLRequest, String> {
  override fun map(from: GraphQLRequest): String {
    val graphQLRequestEntity = GraphQLRequestEntity(from.graphQLValue)
    return json.encodeToString(serializer, graphQLRequestEntity)
  }
}
