package com.magicbell.sdk.feature.store

import com.magicbell.sdk.feature.notification.Notification
import com.magicbell.sdk.feature.notification.motherobject.NotificationMotherObject
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import org.junit.jupiter.api.Test

class NotificationValidatorTests {
  private fun allNotifications(
    read: Boolean? = null,
    seen: Boolean? = null,
    archived: Boolean = false,
    category: String? = null,
    topic: String? = null
  ): List<Notification> {
    val readValues = if (read != null) {
      arrayOf(read)
    } else {
      if (seen != null && seen == false) {
        arrayOf(false)
      } else {
        arrayOf(true, false)
      }
    }

    val seenValues = if (seen != null) {
      arrayOf(seen)
    } else {
      arrayOf(true, false)
    }

    val archivedValues = arrayOf(archived)

    val categoryValues = if (category != null) {
      arrayOf(category)
    } else {
      arrayOf("null", "category")
    }

    val topicValues = if (topic != null) {
      arrayOf(topic)
    } else {
      arrayOf("null", "topic")
    }

    val notifications = mutableListOf<Notification>()

    for (readValue in readValues) {
      for (seenValue in seenValues) {
        for (archivedValue in archivedValues) {
          for (categoryValue in categoryValues) {
            for (topicValue in topicValues) {
              notifications.add(
                NotificationMotherObject.createNotification(
                  read = readValue,
                  seen = seenValue,
                  archived = archivedValue,
                  category = if (categoryValue == "null") null else categoryValue,
                  topic = if (topicValue == "null") null else topicValue
                )
              )
            }
          }
        }
      }
    }

    return notifications
  }

  @Test
  fun test_predicate_all() {
    val predicate = StorePredicate()
    for (notification in allNotifications()) {
      predicate.match(notification).shouldBeTrue()
    }
  }

  @Test
  fun test_predicate_read() {
    val predicate = StorePredicate(read = true)
    for (notification in allNotifications(read = true)) {
      predicate.match(notification).shouldBeTrue()
    }
    for (notification in allNotifications(read = false)) {
      predicate.match(notification).shouldBeFalse()
    }
  }

  @Test
  fun test_predicate_unread() {
    val predicate = StorePredicate(read = false)
    for (notification in allNotifications(read = false)) {
      predicate.match(notification).shouldBeTrue()
    }
    for (notification in allNotifications(read = true)) {
      predicate.match(notification).shouldBeFalse()
    }
  }

  @Test
  fun test_predicate_seen() {
    val predicate = StorePredicate(seen = true)
    for (notification in allNotifications(seen = true)) {
      predicate.match(notification).shouldBeTrue()
    }
    for (notification in allNotifications(seen = false)) {
      predicate.match(notification).shouldBeFalse()
    }
  }

  @Test
  fun test_predicate_unseen() {
    val predicate = StorePredicate(seen = false)
    for (notification in allNotifications(seen = false)) {
      predicate.match(notification).shouldBeTrue()
    }
    for (notification in allNotifications(seen = true)) {
      predicate.match(notification).shouldBeFalse()
    }
  }

  @Test
  fun test_predicate_archived() {
    val predicate = StorePredicate(archived = true)
    for (notification in allNotifications(archived = true)) {
      predicate.match(notification).shouldBeTrue()
    }
    for (notification in allNotifications(archived = false)) {
      predicate.match(notification).shouldBeFalse()
    }
  }

  @Test
  fun test_predicate_unarchived() {
    val predicate = StorePredicate(archived = false)
    for (notification in allNotifications(archived = false)) {
      predicate.match(notification).shouldBeTrue()
    }
    for (notification in allNotifications(archived = true)) {
      predicate.match(notification).shouldBeFalse()
    }
  }

  @Test
  fun test_predicate_category() {
    val predicate = StorePredicate(categories = listOf("the-category"))
    for (notification in allNotifications(category = "the-category")) {
      predicate.match(notification).shouldBeTrue()
    }
    for (notification in allNotifications(category = "not-the-category")) {
      predicate.match(notification).shouldBeFalse()
    }
    for (notification in allNotifications(category = "nil")) {
      predicate.match(notification).shouldBeFalse()
    }
  }

  @Test
  fun test_predicate_topic() {
    val predicate = StorePredicate(topics = listOf("the-topic"))
    for (notification in allNotifications(topic = "the-topic")) {
      predicate.match(notification).shouldBeTrue()
    }
    for (notification in allNotifications(topic = "not-the-topic")) {
      predicate.match(notification).shouldBeFalse()
    }
    for (notification in allNotifications(topic = "nil")) {
      predicate.match(notification).shouldBeFalse()
    }
  }
}