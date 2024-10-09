package com.magicbell.sdk.feature.notificationpreferences.data

import com.magicbell.sdk.common.error.MappingException
import com.magicbell.sdk.common.error.NetworkException
import com.magicbell.sdk.common.network.HttpClient
import com.magicbell.sdk.common.query.UserQuery
import com.mobilejazz.harmony.data.datasource.GetDataSource
import com.mobilejazz.harmony.data.datasource.PutDataSource
import com.mobilejazz.harmony.data.error.OperationNotAllowedException
import com.mobilejazz.harmony.data.query.Query

internal class NotificationPreferencesNetworkDataSource(
  private val httpClient: HttpClient,
  private val inMapper: NotificationPreferencesEntityToNotificationPreferencesContainerEntityMapper,
  private val outMapper: NotificationPreferencesContainerEntityToNotificationPreferencesEntityMapper,
) : GetDataSource<NotificationPreferencesEntity>, PutDataSource<NotificationPreferencesEntity> {

  override suspend fun get(query: Query): NotificationPreferencesEntity {
    return when (query) {
      is UserQuery -> {
        val request = httpClient.prepareRequest(
          "notification_preferences",
          query.externalId,
          query.email,
          query.hmac,
          HttpClient.HttpMethod.Get(),
          arrayOf(Pair("accept-version", "v2"))
        )

        httpClient.performRequest(request)?.let {
          outMapper.map(it)
        } ?: run {
          throw MappingException(NotificationPreferencesEntity::class.java.name)
        }
      }
      else -> {
        throw OperationNotAllowedException()
      }
    }
  }

  override suspend fun getAll(query: Query): List<NotificationPreferencesEntity> = throw NotImplementedError()

  override suspend fun put(query: Query, value: NotificationPreferencesEntity?): NotificationPreferencesEntity {
    return when (query) {
      is UserQuery -> {
        val notificationPreferencesEntity = value ?: throw NetworkException(-1, "Value cannot be null")

        val request = httpClient.prepareRequest(
          "notification_preferences",
          query.externalId,
          query.email,
          query.hmac,
          HttpClient.HttpMethod.Put(inMapper.map(notificationPreferencesEntity)),
          arrayOf(Pair("accept-version", "v2"))
        )

        httpClient.performRequest(request)?.let {
          outMapper.map(it)
        } ?: run {
          throw MappingException(NotificationPreferencesEntity::class.java.name)
        }
      }
      else -> {
        throw OperationNotAllowedException()
      }
    }
  }

  override suspend fun putAll(query: Query, value: List<NotificationPreferencesEntity>?): List<NotificationPreferencesEntity> = throw NotImplementedError()
}