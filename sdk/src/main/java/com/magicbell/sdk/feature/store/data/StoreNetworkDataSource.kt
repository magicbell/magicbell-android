package com.magicbell.sdk.feature.store.data

import android.content.Context
import com.mobilejazz.harmony.data.datasource.GetDataSource
import com.mobilejazz.harmony.data.error.OperationNotAllowedException
import com.mobilejazz.harmony.data.query.Query
import com.magicbell.sdk.common.error.MappingException
import com.magicbell.sdk.common.network.HttpClient
import com.magicbell.sdk.common.network.graphql.GraphQLResponse
import com.magicbell.sdk.feature.store.StorePage

internal class StoreNetworkDataSource(
  private val httpClient: HttpClient,
  private val context: Context,
  private val outMapper: GraphQLResponseToStorePageMapper,
) : GetDataSource<StorePage> {
  override suspend fun get(query: Query): StorePage {
    return when (query) {
      is StoreQuery -> {

        val request = httpClient.prepareRequest(
          "notifications",
          query.userQuery.externalId,
          query.userQuery.email,
          query.userQuery.hmac
        )

        httpClient.performRequest(request)?.let {
          outMapper.map(it)
        } ?: run {
          throw MappingException(GraphQLResponse::class.java.name)
        }
      }
      else -> throw OperationNotAllowedException()
    }
  }

  override suspend fun getAll(query: Query): List<StorePage> = throw NotImplementedError()
}