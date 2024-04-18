package com.magicbell.sdk.feature.userpreferences.data

import com.magicbell.sdk.common.error.MappingException
import com.magicbell.sdk.common.error.NetworkException
import com.magicbell.sdk.common.network.HttpClient
import com.magicbell.sdk.common.query.UserQuery
import com.mobilejazz.harmony.data.datasource.GetDataSource
import com.mobilejazz.harmony.data.datasource.PutDataSource
import com.mobilejazz.harmony.data.error.OperationNotAllowedException
import com.mobilejazz.harmony.data.query.Query

internal class UserPreferencesNetworkDataSource(
  private val httpClient: HttpClient,
  private val inMapper: UserPreferencesEntityToUserPreferencesContainerEntityMapper,
  private val outMapper: UserPreferencesContainerEntityToUserPreferencesEntityMapper,
) : GetDataSource<UserPreferencesEntity>, PutDataSource<UserPreferencesEntity> {

  override suspend fun get(query: Query): UserPreferencesEntity {
    return when (query) {
      is UserQuery -> {
        val request = httpClient.prepareRequest(
          "notification_preferences",
          query.externalId,
          query.email,
          query.hmac
        )

        httpClient.performRequest(request)?.let {
          outMapper.map(it)
        } ?: run {
          throw MappingException(UserPreferencesEntity::class.java.name)
        }
      }
      else -> {
        throw OperationNotAllowedException()
      }
    }
  }

  override suspend fun getAll(query: Query): List<UserPreferencesEntity> = throw NotImplementedError()

  override suspend fun put(query: Query, value: UserPreferencesEntity?): UserPreferencesEntity {
    return when (query) {
      is UserQuery -> {
        val userPreferencesEntity = value ?: throw NetworkException(-1, "Value cannot be null")

        val request = httpClient.prepareRequest(
          "/notification_preferences",
          query.externalId,
          query.email,
          query.hmac,
          HttpClient.HttpMethod.Put(inMapper.map(userPreferencesEntity)),
        )

        httpClient.performRequest(request)?.let {
          outMapper.map(it)
        } ?: run {
          throw MappingException(UserPreferencesEntity::class.java.name)
        }
      }
      else -> {
        throw OperationNotAllowedException()
      }
    }
  }

  override suspend fun putAll(query: Query, value: List<UserPreferencesEntity>?): List<UserPreferencesEntity> = throw NotImplementedError()
}