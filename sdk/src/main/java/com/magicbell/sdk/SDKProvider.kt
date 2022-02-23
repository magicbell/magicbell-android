package com.magicbell.sdk

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.magicbell.sdk.common.environment.Environment
import com.magicbell.sdk.common.logger.LogLevel
import com.magicbell.sdk.common.network.DefaultNetworkModule
import com.magicbell.sdk.common.network.NetworkComponent
import com.magicbell.sdk.common.threading.MainThreadExecutor
import com.magicbell.sdk.feature.config.ConfigComponent
import com.magicbell.sdk.feature.config.DefaultConfigModule
import com.magicbell.sdk.feature.notification.DefaultNotificationModule
import com.magicbell.sdk.feature.notification.NotificationComponent
import com.magicbell.sdk.feature.pushsubscription.DefaultPushSubscriptionModule
import com.magicbell.sdk.feature.pushsubscription.PushSubscriptionComponent
import com.magicbell.sdk.feature.realtime.DefaultStoreRealTimeModule
import com.magicbell.sdk.feature.realtime.StoreRealTimeComponent
import com.magicbell.sdk.feature.store.DefaultStoreModule
import com.magicbell.sdk.feature.store.StoreComponent
import com.magicbell.sdk.feature.userpreferences.DefaultUserPreferencesModule
import com.magicbell.sdk.feature.userpreferences.UserPreferencesComponent
import com.mobilejazz.harmony.common.logger.Logger
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
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
  private val coroutineScope: CoroutineScope,
) : SDKComponent {

  // Components
  private val networkComponent: NetworkComponent by lazy {
    DefaultNetworkModule(logLevel, environment)
  }

  private val coroutinesComponent: CoroutinesComponent by lazy {
    DefaultCoroutinesModule()
  }
  private val configComponent: ConfigComponent by lazy {
    DefaultConfigModule(
      networkComponent.getHttpClient(),
      networkComponent.getJsonSerialization(),
      coroutinesComponent.coroutineDispatcher,
      context.getSharedPreferences("magicbell-sdk", Context.MODE_PRIVATE),
    )
  }
  private val notificationComponent: NotificationComponent by lazy {
    DefaultNotificationModule(
      networkComponent.getHttpClient(),
      networkComponent.getJsonSerialization(),
      coroutinesComponent.coroutineDispatcher
    )
  }
  private val pushSubscriptionComponent: PushSubscriptionComponent by lazy {
    DefaultPushSubscriptionModule(
      networkComponent.getHttpClient(),
      networkComponent.getJsonSerialization(),
      coroutinesComponent.coroutineDispatcher
    )
  }
  private val userPreferencesComponent: UserPreferencesComponent by lazy {
    DefaultUserPreferencesModule(
      networkComponent.getHttpClient(),
      networkComponent.getJsonSerialization(),
      coroutinesComponent.coroutineDispatcher
    )
  }
  private val storeComponent: StoreComponent by lazy {
    DefaultStoreModule(
      networkComponent.getHttpClient(),
      networkComponent.getJsonSerialization(),
      coroutinesComponent.coroutineDispatcher,
      coroutineScope,
      MainThreadExecutor(Handler(Looper.getMainLooper())),
      context,
      notificationComponent,
      storeRealTimeComponent,
      configComponent
    )
  }
  private val storeRealTimeComponent: StoreRealTimeComponent by lazy {
    DefaultStoreRealTimeModule(environment)
  }

  // SDK Component
  override fun getLogger(): Logger = logLevel.logger()

  override fun storeComponent(): StoreComponent = storeComponent

  override fun pushSubscriptionComponent(): PushSubscriptionComponent = pushSubscriptionComponent

  override fun userPreferencesComponent(): UserPreferencesComponent = userPreferencesComponent

  override fun configComponent(): ConfigComponent = configComponent
}

internal interface CoroutinesComponent {
  val coroutineDispatcher: CoroutineDispatcher
}

internal class DefaultCoroutinesModule : CoroutinesComponent {
  override var coroutineDispatcher: CoroutineDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
}