package com.magicbell.sdk.feature.fcmtoken

import com.mobilejazz.harmony.data.datasource.VoidGetDataSource
import com.mobilejazz.harmony.data.repository.SingleDataSourceRepository
import com.mobilejazz.harmony.domain.interactor.toDeleteInteractor
import com.mobilejazz.harmony.domain.interactor.toPutInteractor
import com.magicbell.sdk.common.network.HttpClient
import com.magicbell.sdk.common.query.UserQuery
import com.magicbell.sdk.feature.fcmtoken.data.FCMTokenEntityToFCMTokenMapper
import com.magicbell.sdk.feature.fcmtoken.data.FCMTokenNetworkDataSource
import com.magicbell.sdk.feature.fcmtoken.data.FCMTokenToFCMTokenEntityMapper
import com.magicbell.sdk.feature.fcmtoken.interactor.DeleteFCMTokenInteractor
import com.magicbell.sdk.feature.fcmtoken.interactor.SendFCMTokenInteractor
import kotlinx.serialization.json.Json
import kotlin.coroutines.CoroutineContext

internal interface FCMTokenComponent {
  fun getFCMTokenDirector(userQuery: UserQuery): FCMTokenDirector
}

internal class DefaultFCMTokenModule(
  private val httpClient: HttpClient,
  private val json: Json,
  private val coroutineContext: CoroutineContext,
) : FCMTokenComponent {

  override fun getFCMTokenDirector(userQuery: UserQuery): FCMTokenDirector {
    return DefaultFCMTokenDirector(userQuery, getSendFCMTokenInteractor(), getDeleteFCMTokenInteractor())
  }

  private fun getSendFCMTokenInteractor(): SendFCMTokenInteractor {
    return SendFCMTokenInteractor(coroutineContext, fcmTokenRepository.toPutInteractor(coroutineContext))
  }

  private fun getDeleteFCMTokenInteractor(): DeleteFCMTokenInteractor {
    return DeleteFCMTokenInteractor(coroutineContext, fcmTokenRepository.toDeleteInteractor(coroutineContext))
  }

  private val fcmTokenRepository by lazy {
    val fcmTokenEntitySerializer = FCMTokenEntity.serializer()
    val fcmTokenNetworkDataSource = FCMTokenNetworkDataSource(httpClient,
      FCMTokenEntityToFCMTokenMapper(fcmTokenEntitySerializer, json),
      FCMTokenToFCMTokenEntityMapper(fcmTokenEntitySerializer, json))

    SingleDataSourceRepository(VoidGetDataSource(), fcmTokenNetworkDataSource, fcmTokenNetworkDataSource)
  }
}