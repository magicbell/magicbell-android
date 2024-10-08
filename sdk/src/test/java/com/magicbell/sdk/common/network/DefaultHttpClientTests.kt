package com.magicbell.sdk.common.network

import com.magicbell.sdk.common.environment.Environment
import io.mockk.mockk
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.net.URL

internal class DefaultHttpClientTests {

  private val okHttpClient:OkHttpClient = mockk()
  private val json: Json = mockk()

  @Test
  fun test_prepareRequest_get_queryParameters_appendsSome() {
    // GIVEN
    val environment = Environment("api-key", URL("http://api.magicbell.test"))
    val httpClient = DefaultHttpClient(environment, okHttpClient, json)
    val get = HttpClient.HttpMethod.Get(listOf("key" to "value", "key2" to "value-2"))

    // WHEN
    val request = httpClient.prepareRequest("the-pointy-end", null, null, null, get)

    // THEN
    assertEquals(request.url.toString(), "http://api.magicbell.test/the-pointy-end?key=value&key2=value-2")
  }

  @Test
  fun test_prepareRequest_get_queryParameters_supportsMultipleValuesForSameKey() {
    // GIVEN
    val environment = Environment("api-key", URL("http://api.magicbell.test"))
    val httpClient = DefaultHttpClient(environment, okHttpClient, json)
    val get = HttpClient.HttpMethod.Get(listOf("key" to "value", "key" to "another-value"))

    // WHEN
    val request = httpClient.prepareRequest("the-pointy-end", null, null, null, get)

    // THEN
    assertEquals(request.url.toString(), "http://api.magicbell.test/the-pointy-end?key=value&key=another-value")
  }
}