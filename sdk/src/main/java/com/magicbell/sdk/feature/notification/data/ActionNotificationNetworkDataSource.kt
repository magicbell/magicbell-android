package com.magicbell.sdk.feature.notification.data

import com.magicbell.sdk.common.network.HttpClient
import com.mobilejazz.harmony.data.datasource.DeleteDataSource
import com.mobilejazz.harmony.data.datasource.PutDataSource
import com.mobilejazz.harmony.data.error.OperationNotAllowedException
import com.mobilejazz.harmony.data.query.Query

internal class ActionNotificationNetworkDataSource(
  private val httpClient: HttpClient,
) : PutDataSource<Unit>, DeleteDataSource {
  override suspend fun put(query: Query, value: Unit?) {
    when (query) {
      is NotificationActionQuery -> {
        var path = "notifications"
        var httpMethod: HttpClient.HttpMethod = HttpClient.HttpMethod.Post()
        when (query.action) {
          NotificationActionQuery.Action.MARK_AS_READ -> {
            path += "/${query.notificationId}/read"
          }
          NotificationActionQuery.Action.MARK_AS_UNREAD -> {
            path += "/${query.notificationId}/unread"
          }
          NotificationActionQuery.Action.ARCHIVE -> {
            path += "/${query.notificationId}/archive"
          }
          NotificationActionQuery.Action.UNARCHIVE -> {
            path += "/${query.notificationId}/archive"
            httpMethod = HttpClient.HttpMethod.Delete
          }
          NotificationActionQuery.Action.MARK_ALL_AS_READ -> {
            path += "/read"
          }
          NotificationActionQuery.Action.MARK_ALL_AS_SEEN -> {
            path += "/seen"
          }
        }
        val request = httpClient.prepareRequest(path, query.userQuery.externalId, query.userQuery.email, httpMethod)
        httpClient.performRequest(request)
        return
      }
      else -> {
        throw OperationNotAllowedException()
      }
    }
  }

  override suspend fun putAll(query: Query, value: List<Unit>?): List<Unit> = throw NotImplementedError()

  override suspend fun delete(query: Query) {
    when (query) {
      is NotificationQuery -> {
        val request = httpClient.prepareRequest(
          "notifications/${query.notificationId}",
          query.userQuery.externalId,
          query.userQuery.email,
          HttpClient.HttpMethod.Delete
        )
        httpClient.performRequest(request)
        return
      }
      else -> throw  OperationNotAllowedException()
    }
  }
}