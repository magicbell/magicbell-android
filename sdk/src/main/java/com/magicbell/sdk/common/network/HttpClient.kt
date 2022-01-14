package com.magicbell.sdk.common.network

import com.magicbell.sdk.common.environment.Environment
import com.magicbell.sdk.common.error.NetworkErrorEntity
import com.magicbell.sdk.common.error.NetworkException
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

internal interface HttpClient {
  fun prepareRequest(
    path: String,
    externalId: String?,
    email: String?,
    httpMethod: String = "GET",
    postBody: String? = null,
  ): Request

  suspend fun performRequest(request: Request): String?
}

internal class DefaultHttpClient(
  private val environment: Environment,
  private val okHttpClient: OkHttpClient,
  private val json: Json,
) : HttpClient {

  override fun prepareRequest(path: String, externalId: String?, email: String?, httpMethod: String, postBody: String?): Request {
    val request = Request.Builder()
      .url("${environment.baseUrl}/$path")
      .addHeader("X-MAGICBELL-API-KEY", environment.apiKey)

    if (environment.isHMACEnabled && environment.apiSecret != null) {
      addHMACHeader(environment.apiSecret, externalId, email, request)
    }

    addExternalIdOrEmailHeader(externalId, email, request)

    request.method(httpMethod, postBody?.toRequestBody())

    return request.build()
  }

  private fun addHMACHeader(apiSecret: String, externalId: String?, email: String?, request: Request.Builder) {
    if (externalId != null) {
      val hmac = externalId.hmac(apiSecret)
      request.addHeader("X-MAGICBELL-USER-HMAC", hmac)
    } else if (email != null) {
      val hmac = email.hmac(apiSecret)
      request.addHeader("X-MAGICBELL-USER-HMAC", hmac)
    }
  }

  private fun addExternalIdOrEmailHeader(externalId: String?, email: String?, request: Request.Builder) {
    externalId?.also {
      request.addHeader("X-MAGICBELL-USER-EXTERNAL-ID", it)
    }
    email?.also {
      request.addHeader("X-MAGICBELL-USER-EMAIL", it)
    }
  }

  override suspend fun performRequest(request: Request): String? {
    okHttpClient.newCall(request).execute().use { response ->
      if (!response.isSuccessful) {
        response.body?.string()?.let {
          val errorMessage = json.decodeFromString(NetworkErrorEntity.serializer(), it)
          throw NetworkException(response.code, errorMessage.getErrorMessage(NetworkException.defaultErrorMessage))
        } ?: run {
          throw NetworkException(response.code)
        }
      }

      return response.body?.string()
    }
  }
}