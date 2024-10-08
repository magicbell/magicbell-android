package com.magicbell.sdk.feature.store

import com.magicbell.sdk.common.network.HttpClient
import com.magicbell.sdk.common.query.UserQuery
import com.magicbell.sdk.common.threading.MainThread
import com.magicbell.sdk.feature.config.ConfigComponent
import com.magicbell.sdk.feature.notification.NotificationComponent
import com.magicbell.sdk.feature.realtime.StoreRealTimeComponent
import com.magicbell.sdk.feature.store.data.StoreNetworkDataSource
import com.magicbell.sdk.feature.store.data.StoreResponseToStorePageMapper
import com.magicbell.sdk.feature.store.interactor.FetchStorePageDefaultInteractor
import com.magicbell.sdk.feature.store.interactor.FetchStorePageInteractor
import com.magicbell.sdk.feature.store.interactor.GetStorePagesInteractor
import com.mobilejazz.harmony.data.repository.SingleGetDataSourceRepository
import com.mobilejazz.harmony.domain.interactor.toGetInteractor
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.json.Json
import kotlin.coroutines.CoroutineContext

internal interface StoreComponent {
  fun storeDirector(userQuery: UserQuery): InternalStoreDirector
}

internal class DefaultStoreModule(
  private val httpClient: HttpClient,
  private val json: Json,
  private val coroutineContext: CoroutineContext,
  private val coroutineScope: CoroutineScope,
  private val mainThread: MainThread,
  private val notificationComponent: NotificationComponent,
  private val storeRealTimeComponent: StoreRealTimeComponent,
  private val configComponent: ConfigComponent,
) : StoreComponent {

  private val storeNotificationRepository by lazy {
    SingleGetDataSourceRepository(
      StoreNetworkDataSource(
        httpClient,
        StoreResponseToStorePageMapper(StorePage.serializer(), json)
      )
    )
  }

  private fun getStorePagesInteractor(): GetStorePagesInteractor {
    return GetStorePagesInteractor(coroutineContext, storeNotificationRepository.toGetInteractor(coroutineContext))
  }

  private fun getFetchStorePageInteractor(): FetchStorePageInteractor {
    return FetchStorePageDefaultInteractor(coroutineContext, getStorePagesInteractor())
  }

  override fun storeDirector(userQuery: UserQuery): InternalStoreDirector {
    return RealTimeByPredicateStoreDirector(
      userQuery,
      coroutineContext,
      coroutineScope,
      mainThread,
      getFetchStorePageInteractor(),
      notificationComponent.getActionNotificationInteractor(),
      notificationComponent.getDeleteNotificationInteractor(),
      configComponent.getGetConfigInteractor(),
      configComponent.getDeleteConfigInteractor(),
      storeRealTimeComponent.createStoreRealTime(userQuery)
    )
  }
}