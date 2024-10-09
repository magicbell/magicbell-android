package com.magicbell.sdk.feature.store.data

import com.magicbell.sdk.feature.store.StoreContext
import com.magicbell.sdk.feature.store.StorePagePredicate
import com.magicbell.sdk.feature.store.StorePredicate
import io.kotest.matchers.equality.shouldBeEqualToComparingFields
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class StoreContextTests {
  private val defaultPagePredicate = StorePagePredicate(1, 1)

  private val defaultQueryParameters = listOf(
    "archived" to "false",
    "page" to "${defaultPagePredicate.page}",
    "per_page" to "${defaultPagePredicate.size}")


  @Test
  fun test_asQueryParameter_defaultPredicate() {
    // GIVEN
    val predicate = StorePredicate()
    val storeContext = StoreContext(predicate, defaultPagePredicate)

    // WHEN
    val result = storeContext.asQueryParameters()

    // THEN
    val expected = defaultQueryParameters
    // compare content, not order
    assertEquals( result.sortedBy { it.first }, expected.sortedBy { it.first } )
  }

  @Test
  fun test_asQueryParameter_withReadPredicate() {
    // GIVEN
    val predicate = StorePredicate(read = true)
    val storeContext = StoreContext(predicate, defaultPagePredicate)

    // WHEN
    val result = storeContext.asQueryParameters()

    // THEN
    val expected = defaultQueryParameters + listOf("read" to "true")
    // compare content, not order
    assertEquals( result.sortedBy { it.first }, expected.sortedBy { it.first } )
  }

  @Test
  fun test_asQueryParameter_withUnreadPredicate() {
    // GIVEN
    val predicate = StorePredicate(read = false)
    val storeContext = StoreContext(predicate, defaultPagePredicate)

    // WHEN
    val result = storeContext.asQueryParameters()

    // THEN
    val expected = defaultQueryParameters + listOf("read" to "false")
    // compare content, not order
    assertEquals( result.sortedBy { it.first }, expected.sortedBy { it.first } )
  }

  @Test
  fun test_asQueryParameter_withSeenPredicate() {
    // GIVEN
    val predicate = StorePredicate(seen = true)
    val storeContext = StoreContext(predicate, defaultPagePredicate)

    // WHEN
    val result = storeContext.asQueryParameters()

    // THEN
    val expected = defaultQueryParameters + listOf("seen" to "true")
    // compare content, not order
    assertEquals( result.sortedBy { it.first }, expected.sortedBy { it.first } )
  }

  @Test
  fun test_asQueryParameter_withUnseenPredicate() {
    // GIVEN
    val predicate = StorePredicate(seen = false)
    val storeContext = StoreContext(predicate, defaultPagePredicate)

    // WHEN
    val result = storeContext.asQueryParameters()

    // THEN
    val expected = defaultQueryParameters + listOf("seen" to "false")
    // compare content, not order
    assertEquals( result.sortedBy { it.first }, expected.sortedBy { it.first } )
  }

  @Test
  fun test_asQueryParameter_withPagePredicate() {
    // GIVEN
    val predicate = StorePredicate()
    val pagePredicate = StorePagePredicate(3, 77)
    val storeContext = StoreContext(predicate, pagePredicate)

    // WHEN
    val result = storeContext.asQueryParameters()

    // THEN
    val expected = listOf("archived" to "false", "page" to "3", "per_page" to "77")
    // compare content, not order
    assertEquals( result.sortedBy { it.first }, expected.sortedBy { it.first } )
  }

  @Test
  fun test_asQueryParameter_withTopicFilter() {
    // GIVEN
    val predicate = StorePredicate(topic = "example")
    val storeContext = StoreContext(predicate, defaultPagePredicate)

    // WHEN
    val result = storeContext.asQueryParameters()

    // THEN
    val expected = defaultQueryParameters + listOf("topic" to "example")
    // compare content, not order
    assertEquals( result.sortedBy { it.first }, expected.sortedBy { it.first } )
  }

  @Test
  fun test_asQueryParameter_withCategoryFilter() {
    // GIVEN
    val predicate = StorePredicate(category = "example")
    val storeContext = StoreContext(predicate, defaultPagePredicate)

    // WHEN
    val result = storeContext.asQueryParameters()

    // THEN
    val expected = defaultQueryParameters + listOf("category" to "example")
    // compare content, not order
    assertEquals( result.sortedBy { it.first }, expected.sortedBy { it.first } )
  }

  @Test
  fun test_asQueryParameter_withComplexPredicateAndPage() {
    // GIVEN
    val predicate = StorePredicate(seen = false, read = true, archived = true, topic = "a-topic", category = "a-category")
    val pagePredicate = StorePagePredicate(9, 37)
    val storeContext = StoreContext(predicate, pagePredicate)

    // WHEN
    val result = storeContext.asQueryParameters()

    // THEN
    val expected = listOf(
      "seen" to "false", "read" to "true", "archived" to "true",
      "topic" to "a-topic", "category" to "a-category",
      "page" to "9", "per_page" to "37")
    // compare content, not order
    assertEquals( result.sortedBy { it.first }, expected.sortedBy { it.first } )
  }
}