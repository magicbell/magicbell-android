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
import com.magicbell.sdk.feature.fcmtoken.DefaultFCMTokenModule
import com.magicbell.sdk.feature.fcmtoken.FCMTokenComponent
import com.magicbell.sdk.feature.realtime.DefaultStoreRealTimeModule
import com.magicbell.sdk.feature.realtime.StoreRealTimeComponent
import com.magicbell.sdk.feature.store.DefaultStoreModule
import com.magicbell.sdk.feature.store.StoreComponent
import com.magicbell.sdk.feature.notificationpreferences.DefaultNotificationPreferencesModule
import com.magicbell.sdk.feature.notificationpreferences.NotificationPreferencesComponent
import com.mobilejazz.harmony.common.logger.Logger
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors

internal interface SDKComponent {
  fun getLogger(): Logger
  fun storeComponent(): StoreComponent
  fun fcmTokenComponent(): FCMTokenComponent
  fun userPreferencesComponent(): NotificationPreferencesComponent
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
  private val FCMTokenComponent: FCMTokenComponent by lazy {
    DefaultFCMTokenModule(
      networkComponent.getHttpClient(),
      networkComponent.getJsonSerialization(),
      coroutinesComponent.coroutineDispatcher
    )
  }
  private val notificationPreferencesComponent: NotificationPreferencesComponent by lazy {
    DefaultNotificationPreferencesModule(
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

  override fun fcmTokenComponent(): FCMTokenComponent = FCMTokenComponent

  override fun userPreferencesComponent(): NotificationPreferencesComponent = notificationPreferencesComponent

  override fun configComponent(): ConfigComponent = configComponent
}

internal interface CoroutinesComponent {
  val coroutineDispatcher: CoroutineDispatcher
}

internal class DefaultCoroutinesModule : CoroutinesComponent {
  override var coroutineDispatcher: CoroutineDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
}