package com.magicbell.sdk.common.network

import com.magicbell.sdk.common.environment.Environment
import com.magicbell.sdk.common.logger.LogLevel
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

internal interface NetworkComponent {
  fun getHttpClient(): HttpClient
  fun getJsonSerialization(): Json
}

internal class DefaultNetworkModule(logLevel: LogLevel, environment: Environment) : NetworkComponent {

  override fun getJsonSerialization(): Json {
    return json
  }
  @OptIn(ExperimentalSerializationApi::class)
  private val json: Json by lazy {
    Json {
      ignoreUnknownKeys = true
      explicitNulls = false
    }
  }

  override fun getHttpClient(): HttpClient = httpClient

  private val httpClient by lazy {
    val okHttpClient = OkHttpClient.Builder()
    if (logLevel == LogLevel.DEBUG) {
      okHttpClient.addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
    }
    okHttpClient.followRedirects(false)
    okHttpClient.cache(null)

    DefaultHttpClient(
      environment,
      okHttpClient.build(),
      json
    )
  }
}