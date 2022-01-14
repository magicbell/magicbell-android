package com.magicbell.sdk

import android.content.Context
import com.harmony.kotlin.common.logger.Logger
import com.magicbell.sdk.common.environment.Environment
import com.magicbell.sdk.common.logger.LogLevel
import com.magicbell.sdk.common.logger.LogLevel.DEBUG
import com.magicbell.sdk.common.network.DefaultHttpClient
import com.magicbell.sdk.common.network.HttpClient
import com.magicbell.sdk.feature.config.ConfigComponent
import com.magicbell.sdk.feature.config.DefaultConfigModule
import com.magicbell.sdk.feature.notification.DefaultNotificationModule
import com.magicbell.sdk.feature.notification.NotificationComponent
import com.magicbell.sdk.feature.pushsubscription.DefaultPushSubscriptionModule
import com.magicbell.sdk.feature.pushsubscription.PushSubscriptionComponent
import com.magicbell.sdk.feature.store.DefaultStoreModule
import com.magicbell.sdk.feature.store.StoreComponent
import com.magicbell.sdk.feature.userpreferences.DefaultUserPreferencesModule
import com.magicbell.sdk.feature.userpreferences.UserPreferencesComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.Executors

internal interface SDKComponent {
  fun getLogger(): Logger
  fun storeComponent(): StoreComponent
  fun pushSubscriptionComponent(): PushSubscriptionComponent
  fun userPreferencesComponent(): UserPreferencesComponent
  fun configComponent(): ConfigComponent
}

internal class DefaultSDKModule(
  private val environment: Environment,
  private val logLevel: LogLevel,
  private val context: Context,
) : SDKComponent {

  private val json: Json by lazy {
    Json { ignoreUnknownKeys = true }
  }

  private val httpClient: HttpClient by lazy {
    val okHttpClient = OkHttpClient.Builder()
    if (logLevel == DEBUG) {
      okHttpClient.addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
    }
    okHttpClient.followRedirects(false)

    DefaultHttpClient(
      environment,
      okHttpClient.build(),
      json
    )
  }

  // Components
  private val coroutinesComponent: CoroutinesComponent by lazy {
    DefaultCoroutinesModule()
  }
  private val configComponent: ConfigComponent by lazy {
    DefaultConfigModule(
      httpClient,
      json,
      coroutinesComponent.coroutineDispatcher,
      context.getSharedPreferences("magicbell-sdk", Context.MODE_PRIVATE),
    )
  }
  private val notificationComponent: NotificationComponent by lazy {
    DefaultNotificationModule(httpClient, json, coroutinesComponent.coroutineDispatcher)
  }
  private val pushSubscriptionComponent: PushSubscriptionComponent by lazy {
    DefaultPushSubscriptionModule(httpClient, json, coroutinesComponent.coroutineDispatcher)
  }
  private val userPreferencesComponent: UserPreferencesComponent by lazy {
    DefaultUserPreferencesModule(httpClient, json, coroutinesComponent.coroutineDispatcher)
  }
  private val storeComponent: StoreComponent by lazy {
    DefaultStoreModule(httpClient, json, coroutinesComponent.coroutineDispatcher, context, notificationComponent, configComponent)
  }

  // SDK Component
  override fun getLogger(): Logger = logLevel.logger()

  override fun storeComponent(): StoreComponent = storeComponent

  override fun pushSubscriptionComponent(): PushSubscriptionComponent = pushSubscriptionComponent

  override fun userPreferencesComponent(): UserPreferencesComponent = userPreferencesComponent

  override fun configComponent(): ConfigComponent = configComponent
}

interface CoroutinesComponent {
  val coroutineDispatcher: CoroutineDispatcher
}

class DefaultCoroutinesModule : CoroutinesComponent {
  override var coroutineDispatcher: CoroutineDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
}