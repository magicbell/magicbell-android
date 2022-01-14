package com.magicbell.sdk.common.network.graphql

import kotlinx.serialization.Serializable


@Serializable
internal class GraphQLRequestEntity(
  val query: String,
)

internal class GraphQLRequest(
  private val fragment: GraphQLFragment,
  private val predicates: List<GraphQLRepresentable>,
) : GraphQLRepresentable {

  constructor(fragment: GraphQLFragment, predicate: GraphQLRepresentable) : this(fragment, listOf(predicate))

  override val graphQLValue: String
    get() {
      var query = "query {"
      query += predicates.joinToString("\n ") { it.graphQLValue }
      query += "} "
      query += fragment.graphQLValue
      return query
    }
}