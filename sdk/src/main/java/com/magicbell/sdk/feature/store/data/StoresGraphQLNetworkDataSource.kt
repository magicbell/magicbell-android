package com.magicbell.sdk.feature.store.data

import android.content.Context
import com.harmony.kotlin.data.datasource.GetDataSource
import com.harmony.kotlin.data.error.OperationNotAllowedException
import com.harmony.kotlin.data.query.Query
import com.magicbell.sdk.common.error.MappingException
import com.magicbell.sdk.common.network.HttpClient
import com.magicbell.sdk.common.network.graphql.GraphQLFragment
import com.magicbell.sdk.common.network.graphql.GraphQLRequest
import com.magicbell.sdk.common.network.graphql.GraphQLResponse
import com.magicbell.sdk.feature.store.StorePage

internal class StoresGraphQLNetworkDataSource(
  private val httpClient: HttpClient,
  private val context: Context,
  private val inMapper: GraphQLRequestToGraphQLEntityMapper,
  private val outMapper: GraphQLResponseToStorePageMapper,
) : GetDataSource<Map<String, StorePage>> {
  override suspend fun get(query: Query): Map<String, StorePage> {
    return when (query) {
      is StoreQuery -> {
        val graphQLRequest = GraphQLRequest(
          GraphQLFragment("NotificationFragment", context),
          query
        )

        val request = httpClient.prepareRequest("graphql",
          query.userQuery.externalId,
          query.userQuery.email,
          "POST",
          inMapper.map(graphQLRequest))

        httpClient.performRequest(request)?.let {
          outMapper.map(it)
        } ?: run {
          throw MappingException(GraphQLResponse::class.java.name)
        }
      }
      else -> throw OperationNotAllowedException()
    }
  }

  override suspend fun getAll(query: Query): List<Map<String, StorePage>> = throw NotImplementedError()
}