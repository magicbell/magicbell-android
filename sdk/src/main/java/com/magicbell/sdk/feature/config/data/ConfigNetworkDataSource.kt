package com.magicbell.sdk.feature.config.data

import com.harmony.kotlin.data.datasource.GetDataSource
import com.harmony.kotlin.data.error.MappingException
import com.harmony.kotlin.data.error.QueryNotSupportedException
import com.harmony.kotlin.data.query.Query
import com.magicbell.sdk.common.network.HttpClient
import com.magicbell.sdk.common.network.StringToEntityMapper
import com.magicbell.sdk.common.query.UserQuery
import com.magicbell.sdk.feature.config.Config

internal class ConfigNetworkDataSource(
  private val httpClient: HttpClient,
  private val mapper: StringToEntityMapper<Config>,
) : GetDataSource<Config> {

  override suspend fun get(query: Query): Config {
    when (query) {
      is UserQuery -> {
        val urlConnection = httpClient.prepareRequest("/config",
          query.externalId,
          query.email
        )

        return httpClient.performRequest(urlConnection)?.let {
          mapper.map(it)
        } ?: run {
          throw MappingException()
        }
      }
      else -> throw QueryNotSupportedException()
    }
  }

  override suspend fun getAll(query: Query) = throw NotImplementedError()
}