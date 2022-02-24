package com.magicbell.sdk.feature.pushsubscription

import com.mobilejazz.harmony.data.datasource.VoidGetDataSource
import com.mobilejazz.harmony.data.repository.SingleDataSourceRepository
import com.mobilejazz.harmony.domain.interactor.toDeleteInteractor
import com.mobilejazz.harmony.domain.interactor.toPutInteractor
import com.magicbell.sdk.common.network.HttpClient
import com.magicbell.sdk.common.query.UserQuery
import com.magicbell.sdk.feature.pushsubscription.data.PushSubscriptionEntityToPushSubscriptionMapper
import com.magicbell.sdk.feature.pushsubscription.data.PushSubscriptionNetworkDataSource
import com.magicbell.sdk.feature.pushsubscription.data.PushSubscriptionToPushSubscriptionEntityMapper
import com.magicbell.sdk.feature.pushsubscription.interactor.DeletePushSubscriptionInteractor
import com.magicbell.sdk.feature.pushsubscription.interactor.SendPushSubscriptionInteractor
import kotlinx.serialization.json.Json
import kotlin.coroutines.CoroutineContext

internal interface PushSubscriptionComponent {
  fun getPushSubscriptionDirector(userQuery: UserQuery): PushSubscriptionDirector
}

internal class DefaultPushSubscriptionModule(
  private val httpClient: HttpClient,
  private val json: Json,
  private val coroutineContext: CoroutineContext,
) : PushSubscriptionComponent {

  override fun getPushSubscriptionDirector(userQuery: UserQuery): PushSubscriptionDirector {
    return DefaultPushSubscriptionDirector(userQuery, getSendPushSubscriptionInteractor(), getDeletePushSubscriptionInteractor())
  }

  private fun getSendPushSubscriptionInteractor(): SendPushSubscriptionInteractor {
    return SendPushSubscriptionInteractor(coroutineContext, pushSubscriptionRepository.toPutInteractor(coroutineContext))
  }

  private fun getDeletePushSubscriptionInteractor(): DeletePushSubscriptionInteractor {
    return DeletePushSubscriptionInteractor(coroutineContext, pushSubscriptionRepository.toDeleteInteractor(coroutineContext))
  }

  private val pushSubscriptionRepository by lazy {
    val pushSubscriptionEntitySerializer = PushSubscriptionEntity.serializer()
    val pushSubscriptionNetworkDataSource = PushSubscriptionNetworkDataSource(httpClient,
      PushSubscriptionEntityToPushSubscriptionMapper(pushSubscriptionEntitySerializer, json),
      PushSubscriptionToPushSubscriptionEntityMapper(pushSubscriptionEntitySerializer, json))

    SingleDataSourceRepository(VoidGetDataSource(), pushSubscriptionNetworkDataSource, pushSubscriptionNetworkDataSource)
  }
}