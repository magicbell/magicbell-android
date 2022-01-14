package com.magicbell.sdk.feature.store

import com.magicbell.sdk.common.network.graphql.GraphQLRepresentable
import com.magicbell.sdk.feature.notification.Notification

data class StorePredicate(
  val read: Boolean? = null,
  val seen: Boolean? = null,
  val archived: Boolean = false,
  val categories: List<String> = listOf(),
  val topics: List<String> = listOf(),
) : GraphQLRepresentable {
  override val graphQLValue: String
    get() {
      val storePredicateParams = mutableListOf<String>()

      read?.also {
        if (it) {
          storePredicateParams.add("read: true")
        } else {
          storePredicateParams.add("read: false")
        }
      }

      seen?.also {
        if (it) {
          storePredicateParams.add("seen: true")
        } else {
          storePredicateParams.add("seen: false")
        }
      }

      if (archived) {
        storePredicateParams.add("archived: true")
      } else {
        storePredicateParams.add("archived: false")
      }

      if (categories.isNotEmpty()) {
        storePredicateParams.add("categories:[${categories.joinToString(", ") { "\"$it\"" }}]")
      }

      if (topics.isNotEmpty()) {
        storePredicateParams.add("topics:[${categories.joinToString(", ") { "\"$it\"" }}]")
      }

      return storePredicateParams.joinToString(", ")
    }
}

internal fun StorePredicate.match(notification: Notification): Boolean {
  val validator = NotificationValidator(this)
  return validator.validate(notification)
}
