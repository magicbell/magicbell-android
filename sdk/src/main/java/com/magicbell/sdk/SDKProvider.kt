package com.magicbell.sdk

import android.content.SharedPreferences
import com.harmony.kotlin.common.logger.Logger
import com.magicbell.sdk.common.environment.Environment
import com.magicbell.sdk.common.logger.LogLevel
import com.magicbell.sdk.common.logger.LogLevel.DEBUG
import com.magicbell.sdk.common.network.DefaultHttpClient
import com.magicbell.sdk.common.network.HttpClient
import com.magicbell.sdk.feature.config.ConfigComponent
import com.magicbell.sdk.feature.config.DefaultConfigModule
import com.magicbell.sdk.feature.config.interactor.GetConfigInteractor
import com.magicbell.sdk.feature.notification.DefaultNotificationModule
import com.magicbell.sdk.feature.notification.NotificationComponent
import com.magicbell.sdk.feature.notification.interactor.ActionNotificationInteractor
import com.magicbell.sdk.feature.notification.interactor.DeleteNotificationInteractor
import com.magicbell.sdk.feature.notification.interactor.GetNotificationInteractor
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.Executors

interface SDKComponent {
  fun getLogger(): Logger
  fun getConfigInteractor(): GetConfigInteractor
  fun getActionNotificationInteractor(): ActionNotificationInteractor
  fun getDeleteNotificationInteractor(): DeleteNotificationInteractor
  fun getNotificationInteractor(): GetNotificationInteractor
}

internal class DefaultSDKModule(
  private val environment: Environment,
  private val logLevel: LogLevel,
  private val sharedPreferences: SharedPreferences,
) : SDKComponent {

  override fun getLogger(): Logger = logLevel.logger()

  private val json: Json by lazy {
    Json { ignoreUnknownKeys = true }
  }

  private val httpClient: HttpClient by lazy {
    val okHttpClient = OkHttpClient.Builder()
    if (logLevel == DEBUG) {
      okHttpClient.addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
    }
    okHttpClient.followRedirects(false)

    DefaultHttpClient(
      environment,
      okHttpClient.build(),
      json
    )
  }

// Components

  private val coroutinesComponent: CoroutinesComponent by lazy { DefaultCoroutinesModule() }
  private val configComponent: ConfigComponent by lazy { DefaultConfigModule(httpClient, json, coroutinesComponent.coroutineDispatcher, sharedPreferences) }
  private val notificationComponent: NotificationComponent by lazy { DefaultNotificationModule(httpClient, json, coroutinesComponent.coroutineDispatcher) }

  override fun getConfigInteractor(): GetConfigInteractor {
    return configComponent.getGetConfigInteractor()
  }

  override fun getActionNotificationInteractor(): ActionNotificationInteractor {
    return notificationComponent.getActionNotificationInteractor()
  }

  override fun getDeleteNotificationInteractor(): DeleteNotificationInteractor {
    return notificationComponent.getDeleteNotificationInteractor()
  }

  override fun getNotificationInteractor(): GetNotificationInteractor {
    return notificationComponent.getNotificationInteractor()
  }
}

interface CoroutinesComponent {
  val coroutineDispatcher: CoroutineDispatcher
}

class DefaultCoroutinesModule : CoroutinesComponent {
  override var coroutineDispatcher: CoroutineDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
}