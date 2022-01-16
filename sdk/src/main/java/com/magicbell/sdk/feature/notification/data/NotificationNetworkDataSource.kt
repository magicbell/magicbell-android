package com.magicbell.sdk.feature.notification.data

import com.harmony.kotlin.data.datasource.GetDataSource
import com.harmony.kotlin.data.error.OperationNotAllowedException
import com.harmony.kotlin.data.query.Query
import com.magicbell.sdk.common.error.MappingException
import com.magicbell.sdk.common.network.HttpClient
import com.magicbell.sdk.feature.notification.Notification
import com.magicbell.sdk.feature.notification.NotificationEntity

internal class NotificationNetworkDataSource(
  private val httpClient: HttpClient,
  private val mapper: NotificationEntityToNotificationMapper,
) : GetDataSource<Notification> {
  override suspend fun get(query: Query): Notification {
    when (query) {
      is NotificationQuery -> {
        val request = httpClient.prepareRequest(
          "/notifications/${query.notificationId}",
          query.userQuery.externalId,
          query.userQuery.email
        )

        return httpClient.performRequest(request)?.let {
          mapper.map(it)
        } ?: run {
          throw MappingException(NotificationEntity::class.java.name)
        }
      }
      else -> throw OperationNotAllowedException()
    }

  }

  override suspend fun getAll(query: Query): List<Notification> {
    throw NotImplementedError()
  }
}