package com.magicbell.sdk.feature.notification

import com.harmony.kotlin.data.datasource.VoidGetDataSource
import com.harmony.kotlin.data.repository.SingleDataSourceRepository
import com.harmony.kotlin.data.repository.SingleGetDataSourceRepository
import com.harmony.kotlin.domain.interactor.toDeleteInteractor
import com.harmony.kotlin.domain.interactor.toGetInteractor
import com.harmony.kotlin.domain.interactor.toPutInteractor
import com.magicbell.sdk.common.network.HttpClient
import com.magicbell.sdk.feature.notification.data.ActionNotificationNetworkDataSource
import com.magicbell.sdk.feature.notification.data.NotificationEntityToNotificationMapper
import com.magicbell.sdk.feature.notification.data.NotificationNetworkDataSource
import com.magicbell.sdk.feature.notification.interactor.ActionNotificationDefaultInteractor
import com.magicbell.sdk.feature.notification.interactor.ActionNotificationInteractor
import com.magicbell.sdk.feature.notification.interactor.DeleteNotificationDefaultInteractor
import com.magicbell.sdk.feature.notification.interactor.DeleteNotificationInteractor
import com.magicbell.sdk.feature.notification.interactor.GetNotificationDefaultInteractor
import com.magicbell.sdk.feature.notification.interactor.GetNotificationInteractor
import kotlinx.serialization.json.Json
import kotlin.coroutines.CoroutineContext

internal interface NotificationComponent {
  fun getActionNotificationInteractor(): ActionNotificationInteractor
  fun getDeleteNotificationInteractor(): DeleteNotificationInteractor
  fun getNotificationInteractor(): GetNotificationInteractor
}

internal class DefaultNotificationModule(
  private val httpClient: HttpClient,
  private val json: Json,
  private val coroutineContext: CoroutineContext,
) : NotificationComponent {

  override fun getNotificationInteractor(): GetNotificationInteractor {
    return GetNotificationDefaultInteractor(coroutineContext, notificationRepository.toGetInteractor(coroutineContext))
  }

  private val notificationRepository by lazy {
    SingleGetDataSourceRepository(NotificationNetworkDataSource(httpClient, NotificationEntityToNotificationMapper(NotificationEntity.serializer(), json)))
  }

  override fun getActionNotificationInteractor(): ActionNotificationInteractor {
    return ActionNotificationDefaultInteractor(coroutineContext, actionNotificationRepository.toPutInteractor(coroutineContext))
  }

  override fun getDeleteNotificationInteractor(): DeleteNotificationInteractor {
    return DeleteNotificationDefaultInteractor(coroutineContext, actionNotificationRepository.toDeleteInteractor(coroutineContext))
  }

  private val actionNotificationRepository by lazy {
    val actionNotificationNetworkDataSource = ActionNotificationNetworkDataSource(httpClient)
    SingleDataSourceRepository(VoidGetDataSource(), actionNotificationNetworkDataSource, actionNotificationNetworkDataSource)
  }
}