package com.magicbell.sdk.feature.fcmtoken.data

import com.magicbell.sdk.common.error.MappingException
import com.magicbell.sdk.common.error.NetworkException
import com.magicbell.sdk.common.network.HttpClient
import com.magicbell.sdk.feature.fcmtoken.FCMToken
import com.magicbell.sdk.feature.fcmtoken.FCMTokenEntity
import com.mobilejazz.harmony.data.datasource.DeleteDataSource
import com.mobilejazz.harmony.data.datasource.PutDataSource
import com.mobilejazz.harmony.data.error.OperationNotAllowedException
import com.mobilejazz.harmony.data.query.Query

internal class FCMTokenNetworkDataSource(
  private val httpClient: HttpClient,
  private val outMapper: FCMTokenEntityToFCMTokenMapper,
  private val inMapper: FCMTokenToFCMTokenEntityMapper,
) : PutDataSource<FCMToken>, DeleteDataSource {

  override suspend fun put(query: Query, value: FCMToken?): FCMToken {
    return when (query) {
      is RegisterFCMTokenQuery -> {
        val fcmToken = value ?: throw NetworkException(-1, "Value cannot be null")

        val request = httpClient.prepareRequest(
          "channels/mobile_push/fcm/tokens",
          query.user.externalId,
          query.user.email,
          query.user.hmac,
          HttpClient.HttpMethod.Post(inMapper.map(fcmToken)),
        )

        httpClient.performRequest(request)?.let {
          value
        } ?: run {
          throw MappingException(FCMTokenEntity::class.java.name)
        }
      }
      else -> throw OperationNotAllowedException()
    }
  }

  override suspend fun putAll(query: Query, value: List<FCMToken>?): List<FCMToken> = throw NotImplementedError()

  override suspend fun delete(query: Query) {
    when (query) {
      is DeleteFCMTokenQuery -> {
        val request = httpClient.prepareRequest(
          "channels/mobile_push/fcm/tokens/${query.deviceToken}",
          query.userQuery.externalId,
          query.userQuery.email,
          query.userQuery.hmac,
          HttpClient.HttpMethod.Delete
        )

        httpClient.performRequest(request)
        return
      }
      else -> throw OperationNotAllowedException()
    }
  }
}