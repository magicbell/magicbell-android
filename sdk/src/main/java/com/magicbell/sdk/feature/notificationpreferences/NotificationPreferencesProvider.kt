package com.magicbell.sdk.feature.notificationpreferences

import com.mobilejazz.harmony.data.datasource.VoidDeleteDataSource
import com.mobilejazz.harmony.data.repository.RepositoryMapper
import com.mobilejazz.harmony.data.repository.SingleDataSourceRepository
import com.mobilejazz.harmony.domain.interactor.toGetInteractor
import com.mobilejazz.harmony.domain.interactor.toPutInteractor
import com.magicbell.sdk.common.network.HttpClient
import com.magicbell.sdk.common.query.UserQuery
import com.magicbell.sdk.feature.notificationpreferences.data.NotificationPreferencesContainerEntity
import com.magicbell.sdk.feature.notificationpreferences.data.NotificationPreferencesContainerEntityToNotificationPreferencesEntityMapper
import com.magicbell.sdk.feature.notificationpreferences.data.NotificationPreferencesEntity
import com.magicbell.sdk.feature.notificationpreferences.data.NotificationPreferencesEntityToNotificationPreferencesContainerEntityMapper
import com.magicbell.sdk.feature.notificationpreferences.data.NotificationPreferencesNetworkDataSource
import com.magicbell.sdk.feature.notificationpreferences.interactor.GetNotificationPreferencesInteractor
import com.magicbell.sdk.feature.notificationpreferences.interactor.UpdateNotificationPreferencesInteractor
import kotlinx.serialization.json.Json
import kotlin.coroutines.CoroutineContext

internal interface NotificationPreferencesComponent {
  fun notificationPreferencesDirector(userQuery: UserQuery): NotificationPreferencesDirector
}

internal class DefaultNotificationPreferencesModule(
  private val httpClient: HttpClient,
  private val json: Json,
  private val coroutineContext: CoroutineContext,
) : NotificationPreferencesComponent {

  override fun notificationPreferencesDirector(userQuery: UserQuery): NotificationPreferencesDirector {
    return DefaultNotificationPreferencesDirector(userQuery, getGetUserPreferencesInteractor(), getUpdateUserPreferencesInteractor())
  }

  private val notificationPreferencesRepository: RepositoryMapper<NotificationPreferencesEntity, NotificationPreferences> by lazy {
    val notificationPreferencesNetworkDataSource = NotificationPreferencesNetworkDataSource(
      httpClient,
      NotificationPreferencesEntityToNotificationPreferencesContainerEntityMapper(NotificationPreferencesContainerEntity.serializer(), json),
      NotificationPreferencesContainerEntityToNotificationPreferencesEntityMapper(NotificationPreferencesContainerEntity.serializer(), json)
    )

    val userPreferencesRepository = SingleDataSourceRepository(notificationPreferencesNetworkDataSource, notificationPreferencesNetworkDataSource, VoidDeleteDataSource())

    RepositoryMapper(
      userPreferencesRepository,
      userPreferencesRepository,
      userPreferencesRepository,
      UserPreferencesEntityToUserPreferencesMapper(),
      UserPreferencesToUserPreferencesEntityMapper()
    )
  }

  private fun getGetUserPreferencesInteractor(): GetNotificationPreferencesInteractor {
    return GetNotificationPreferencesInteractor(coroutineContext, notificationPreferencesRepository.toGetInteractor(coroutineContext))
  }

  private fun getUpdateUserPreferencesInteractor(): UpdateNotificationPreferencesInteractor {
    return UpdateNotificationPreferencesInteractor(coroutineContext, notificationPreferencesRepository.toPutInteractor(coroutineContext))
  }
}