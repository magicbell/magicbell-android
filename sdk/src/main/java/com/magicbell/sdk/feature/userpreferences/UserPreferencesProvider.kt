package com.magicbell.sdk.feature.userpreferences

import com.mobilejazz.harmony.data.datasource.VoidDeleteDataSource
import com.mobilejazz.harmony.data.repository.RepositoryMapper
import com.mobilejazz.harmony.data.repository.SingleDataSourceRepository
import com.mobilejazz.harmony.domain.interactor.toGetInteractor
import com.mobilejazz.harmony.domain.interactor.toPutInteractor
import com.magicbell.sdk.common.network.HttpClient
import com.magicbell.sdk.common.query.UserQuery
import com.magicbell.sdk.feature.userpreferences.data.UserPreferencesContainerEntity
import com.magicbell.sdk.feature.userpreferences.data.UserPreferencesContainerEntityToUserPreferencesEntityMapper
import com.magicbell.sdk.feature.userpreferences.data.UserPreferencesEntity
import com.magicbell.sdk.feature.userpreferences.data.UserPreferencesEntityToUserPreferencesContainerEntityMapper
import com.magicbell.sdk.feature.userpreferences.data.UserPreferencesNetworkDataSource
import com.magicbell.sdk.feature.userpreferences.interactor.GetUserPreferencesInteractor
import com.magicbell.sdk.feature.userpreferences.interactor.UpdateUserPreferencesInteractor
import kotlinx.serialization.json.Json
import kotlin.coroutines.CoroutineContext

internal interface UserPreferencesComponent {
  fun userPreferencesDirector(userQuery: UserQuery): UserPreferencesDirector
}

internal class DefaultUserPreferencesModule(
  private val httpClient: HttpClient,
  private val json: Json,
  private val coroutineContext: CoroutineContext,
) : UserPreferencesComponent {

  override fun userPreferencesDirector(userQuery: UserQuery): UserPreferencesDirector {
    return DefaultUserPreferencesDirector(userQuery, getGetUserPreferencesInteractor(), getUpdateUserPreferencesInteractor())
  }

  private val userPreferencesRepository: RepositoryMapper<UserPreferencesEntity, UserPreferences> by lazy {
    val userPreferencesNetworkDataSource = UserPreferencesNetworkDataSource(
      httpClient,
      UserPreferencesEntityToUserPreferencesContainerEntityMapper(UserPreferencesContainerEntity.serializer(), json),
      UserPreferencesContainerEntityToUserPreferencesEntityMapper(UserPreferencesContainerEntity.serializer(), json)
    )

    val userPreferencesRepository = SingleDataSourceRepository(userPreferencesNetworkDataSource, userPreferencesNetworkDataSource, VoidDeleteDataSource())

    RepositoryMapper(
      userPreferencesRepository,
      userPreferencesRepository,
      userPreferencesRepository,
      UserPreferencesEntityToUserPreferencesMapper(),
      UserPreferencesToUserPreferencesEntityMapper()
    )
  }

  private fun getGetUserPreferencesInteractor(): GetUserPreferencesInteractor {
    return GetUserPreferencesInteractor(coroutineContext, userPreferencesRepository.toGetInteractor(coroutineContext))
  }

  private fun getUpdateUserPreferencesInteractor(): UpdateUserPreferencesInteractor {
    return UpdateUserPreferencesInteractor(coroutineContext, userPreferencesRepository.toPutInteractor(coroutineContext))
  }
}