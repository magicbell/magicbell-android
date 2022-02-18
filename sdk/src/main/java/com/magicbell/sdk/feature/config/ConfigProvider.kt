package com.magicbell.sdk.feature.config

import android.content.SharedPreferences
import com.harmony.kotlin.android.data.datasource.DeviceStorageDataSource
import com.harmony.kotlin.data.datasource.DataSourceMapper
import com.harmony.kotlin.data.datasource.VoidDataSource
import com.harmony.kotlin.data.datasource.VoidDeleteDataSource
import com.harmony.kotlin.data.repository.CacheRepository
import com.harmony.kotlin.domain.interactor.toDeleteInteractor
import com.harmony.kotlin.domain.interactor.toGetInteractor
import com.magicbell.sdk.common.network.HttpClient
import com.magicbell.sdk.common.network.StringToEntityMapper
import com.magicbell.sdk.feature.config.data.ConfigNetworkDataSource
import com.magicbell.sdk.feature.config.data.ConfigToStringMapper
import com.magicbell.sdk.feature.config.data.StringToConfigMapper
import com.magicbell.sdk.feature.config.interactor.DeleteConfigDefaultInteractor
import com.magicbell.sdk.feature.config.interactor.GetConfigDefaultInteractor
import com.magicbell.sdk.feature.config.interactor.DeleteConfigInteractor
import com.magicbell.sdk.feature.config.interactor.GetConfigInteractor
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.serialization.json.Json

internal interface ConfigComponent {
  fun getGetConfigInteractor(): GetConfigInteractor
  fun getDeleteConfigInteractor(): DeleteConfigInteractor
}

internal class DefaultConfigModule(
  private val httpClient: HttpClient,
  private val json: Json,
  private val coroutineDispatcher: CoroutineDispatcher,
  private val sharedPreferences: SharedPreferences,
) : ConfigComponent {

  private val userRepository: CacheRepository<Config> by lazy {
    val configNetworkDataSource = ConfigNetworkDataSource(httpClient, StringToEntityMapper(Config.serializer(), json))

    val deviceStorageDataSource = DeviceStorageDataSource<String>(sharedPreferences, "magicbell.config")
    val configDeviceStorage = DataSourceMapper(
      deviceStorageDataSource,
      deviceStorageDataSource,
      deviceStorageDataSource,
      StringToConfigMapper(),
      ConfigToStringMapper()
    )

    CacheRepository(
      configDeviceStorage,
      configDeviceStorage,
      configDeviceStorage,
      configNetworkDataSource,
      VoidDataSource(),
      VoidDeleteDataSource()
    )
  }

  override fun getGetConfigInteractor(): GetConfigInteractor =
    GetConfigDefaultInteractor(coroutineDispatcher, userRepository.toGetInteractor(coroutineDispatcher))

  override fun getDeleteConfigInteractor(): DeleteConfigInteractor =
    DeleteConfigDefaultInteractor(coroutineDispatcher, userRepository.toDeleteInteractor(coroutineDispatcher))
}