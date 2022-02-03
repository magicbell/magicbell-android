package com.magicbell.example

import android.app.Application
import com.magicbell.sdk.MagicBellClient
import com.magicbell.sdk.common.logger.LogLevel

class ExampleApplication : Application() {
  lateinit var magicBellClient: MagicBellClient

  override fun onCreate() {
    super.onCreate()
    magicBellClient = MagicBellClient(
      apiKey = "34ed17a8482e44c765d9e163015a8d586f0b3383",
      logLevel = LogLevel.DEBUG,
      context = applicationContext
    )
  }
}