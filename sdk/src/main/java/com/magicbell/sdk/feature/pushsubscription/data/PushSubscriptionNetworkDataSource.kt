package com.magicbell.sdk.feature.pushsubscription.data

import com.magicbell.sdk.common.error.MappingException
import com.magicbell.sdk.common.error.NetworkException
import com.magicbell.sdk.common.network.HttpClient
import com.magicbell.sdk.feature.pushsubscription.PushSubscription
import com.magicbell.sdk.feature.pushsubscription.PushSubscriptionEntity
import com.mobilejazz.harmony.data.datasource.DeleteDataSource
import com.mobilejazz.harmony.data.datasource.PutDataSource
import com.mobilejazz.harmony.data.error.OperationNotAllowedException
import com.mobilejazz.harmony.data.query.Query

internal class PushSubscriptionNetworkDataSource(
  private val httpClient: HttpClient,
  private val outMapper: PushSubscriptionEntityToPushSubscriptionMapper,
  private val inMapper: PushSubscriptionToPushSubscriptionEntityMapper,
) : PutDataSource<PushSubscription>, DeleteDataSource {

  override suspend fun put(query: Query, value: PushSubscription?): PushSubscription {
    return when (query) {
      is RegisterPushSubscriptionQuery -> {
        val pushSubscription = value ?: throw NetworkException(-1, "Value cannot be null")

        val request = httpClient.prepareRequest(
          "/push_subscriptions",
          query.user.externalId,
          query.user.email,
          query.user.hmac,
          HttpClient.HttpMethod.Post(inMapper.map(pushSubscription)),
        )

        httpClient.performRequest(request)?.let {
          outMapper.map(it)
        } ?: run {
          throw MappingException(PushSubscriptionEntity::class.java.name)
        }
      }
      else -> throw OperationNotAllowedException()
    }
  }

  override suspend fun putAll(query: Query, value: List<PushSubscription>?): List<PushSubscription> = throw NotImplementedError()

  override suspend fun delete(query: Query) {
    when (query) {
      is DeletePushSubscriptionQuery -> {
        val request = httpClient.prepareRequest(
          "/push_subscriptions/${query.deviceToken}",
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