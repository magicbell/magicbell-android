package com.magicbell.sdk

import android.content.Context
import com.magicbell.sdk.common.environment.Environment
import com.magicbell.sdk.common.logger.LogLevel
import java.net.URL

class MagicBell(
  apiKey: String,
  apiSecret: String? = null,
  enableHMAC: Boolean = false,
  baseURL: URL = defaultBaseUrl,
  logLevel: LogLevel = LogLevel.NONE,
  context: Context,
) {

  companion object {
    val defaultBaseUrl = URL("https://api.magicbell.com")
  }

  val sdkComponent: SDKComponent

  init {
    sdkComponent = DefaultSDKModule(
      Environment(apiKey, apiSecret, baseURL, enableHMAC),
      logLevel,
      context.getSharedPreferences("magicbell-sdk", Context.MODE_PRIVATE)
    )
  }
}