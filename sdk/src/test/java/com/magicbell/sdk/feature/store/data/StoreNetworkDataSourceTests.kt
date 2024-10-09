package com.magicbell.sdk.feature.store.data

import com.magicbell.sdk.common.environment.Environment
import com.magicbell.sdk.common.network.DefaultHttpClient
import com.magicbell.sdk.common.network.HttpClient
import com.magicbell.sdk.common.query.UserQuery
import com.magicbell.sdk.feature.store.StoreContext
import com.magicbell.sdk.feature.store.StorePage
import com.magicbell.sdk.feature.store.StorePagePredicate
import com.magicbell.sdk.feature.store.StorePredicate
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import org.junit.jupiter.api.Test
import java.net.URL

internal class StoreNetworkDataSourceTests {

  private val defaultPagePredicate = StorePagePredicate(1, 1)
  private val userQuery = UserQuery.createEmail("john@doe.com")

  private val httpClient: HttpClient = run {
    val m = mockk<HttpClient>()
    coEvery { m.performRequest(any()) } returns "mock response"
    coEvery { m.prepareRequest(any(), any(), any(),any(),any(),any()) } returns mockk<Request>()
    m
  }

  private val defaultMapper: StoreResponseToStorePageMapper = run {
    val m = mockk<StoreResponseToStorePageMapper>()
    every { m.map(any()) } returns mockk<StorePage>()
    m
  }

  @Test
  fun test_queriesCorrectPath() = runBlocking {
    // GIVEN
    val predicate = StorePredicate()
    val storeContext = StoreContext(predicate, defaultPagePredicate)
    val query = StoreQuery(storeContext, userQuery)
    val dataSource = StoreNetworkDataSource(httpClient, defaultMapper)

    // WHEN
    dataSource.get(query)

    // THEN
    coVerify(exactly = 1, timeout = 1000) { httpClient.prepareRequest("notifications", any(), any(), any(), any(), any()) }

    Unit
  }
}