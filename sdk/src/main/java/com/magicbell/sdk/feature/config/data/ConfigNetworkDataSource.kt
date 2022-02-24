package com.magicbell.sdk.feature.config.data

import com.mobilejazz.harmony.data.datasource.GetDataSource
import com.mobilejazz.harmony.data.error.QueryNotSupportedException
import com.mobilejazz.harmony.data.query.Query
import com.magicbell.sdk.common.error.MappingException
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
        val request = httpClient.prepareRequest("config",
          query.externalId,
          query.email
        )

        return httpClient.performRequest(request)?.let {
          mapper.map(it)
        } ?: run {
          throw MappingException(Config::class.java.name)
        }
      }
      else -> throw QueryNotSupportedException()
    }
  }

  override suspend fun getAll(query: Query) = throw NotImplementedError()
}
