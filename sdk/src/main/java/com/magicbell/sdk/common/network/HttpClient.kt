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
    hmac: String?,
    httpMethod: HttpMethod = HttpMethod.Get
  ): Request

  suspend fun performRequest(request: Request): String?

  sealed class HttpMethod(val name: String, val body: String? = null) {
    object Get: HttpMethod("GET")
    class Post(body: String = ""): HttpMethod("POST", body)
    class Put(body: String = ""): HttpMethod("PUT", body)
    object Delete: HttpMethod("DELETE")
  }
}

internal class DefaultHttpClient(
  private val environment: Environment,
  private val okHttpClient: OkHttpClient,
  private val json: Json,
) : HttpClient {

  override fun prepareRequest(path: String, externalId: String?, email: String?, hmac: String?, httpMethod: HttpClient.HttpMethod): Request {
    val request = Request.Builder()
      .url("${environment.baseUrl}/$path")
      .addHeader("X-MAGICBELL-API-KEY", environment.apiKey)

    hmac?.also {
      request.addHeader("X-MAGICBELL-USER-HMAC", it)
    }

    addExternalIdOrEmailHeader(externalId, email, request)
    request.method(httpMethod.name, httpMethod.body?.toRequestBody())
    request.addHeader("Content-Type", "application/json")

    return request.build()
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