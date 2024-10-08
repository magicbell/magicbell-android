package com.magicbell.sdk.feature.store

import android.content.Context
import com.magicbell.sdk.common.network.HttpClient
import com.magicbell.sdk.common.network.graphql.GraphQLRequestEntity
import com.magicbell.sdk.common.network.graphql.GraphQLResponse
import com.magicbell.sdk.common.query.UserQuery
import com.magicbell.sdk.common.threading.MainThread
import com.magicbell.sdk.feature.config.ConfigComponent
import com.magicbell.sdk.feature.notification.NotificationComponent
import com.magicbell.sdk.feature.realtime.StoreRealTimeComponent
import com.magicbell.sdk.feature.store.data.GraphQLRequestToGraphQLEntityMapper
import com.magicbell.sdk.feature.store.data.GraphQLResponseToStorePageMapper
import com.magicbell.sdk.feature.store.data.StoreNetworkDataSource
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
  private val context: Context,
  private val notificationComponent: NotificationComponent,
  private val storeRealTimeComponent: StoreRealTimeComponent,
  private val configComponent: ConfigComponent,
) : StoreComponent {

  private val storeNotificationGraphQLRepository by lazy {
    SingleGetDataSourceRepository(
      StoreNetworkDataSource(
        httpClient,
        context,
        GraphQLRequestToGraphQLEntityMapper(GraphQLRequestEntity.serializer(), json),
        GraphQLResponseToStorePageMapper(GraphQLResponse.serializer(StorePage.serializer()), json)
      )
    )
  }

  private fun getStorePagesInteractor(): GetStorePagesInteractor {
    return GetStorePagesInteractor(coroutineContext, storeNotificationGraphQLRepository.toGetInteractor(coroutineContext))
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