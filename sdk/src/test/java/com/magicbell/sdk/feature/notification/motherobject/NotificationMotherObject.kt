package com.magicbell.sdk.feature.notification.motherobject

import com.mobilejazz.harmony.common.randomBoolean
import com.mobilejazz.harmony.common.randomString
import com.magicbell.sdk.feature.notification.Notification
import com.magicbell.sdk.feature.store.StorePredicate
import java.util.Date

sealed class ForceProperty {
  object None : ForceProperty()
  object Read : ForceProperty()
  object Unread : ForceProperty()
  object Seen : ForceProperty()
  object Unseen : ForceProperty()
  object Archived : ForceProperty()
  object Unarchived : ForceProperty()
}

fun anyNotification(predicate: StorePredicate, id: String?, forceProperty: ForceProperty): Notification {
  return NotificationMotherObject.createNotificationForPredicate(predicate, id, forceProperty)
}

internal class NotificationMotherObject {
  companion object {
    fun createNotificationForPredicate(
      predicate: StorePredicate,
      id: String? = null,
      forceProperty: ForceProperty,
    ): Notification {

      var read: Boolean = if (predicate.read == true) {
        true
      } else if (predicate.read == false) {
        false
      } else {
        randomBoolean()
      }

      var seen: Boolean = if (predicate.seen == true) {
        true
      } else if (predicate.seen == false) {
        false
      } else {
        randomBoolean()
      }

      var archived: Boolean = predicate.archived

      when (forceProperty) {
        ForceProperty.Read -> {
          read = true
          seen = true
        }
        ForceProperty.Unread -> {
          read = false
          seen = false
        }
        ForceProperty.Seen -> seen = true
        ForceProperty.Unseen -> {
          seen = false
          read = false
        }
        ForceProperty.Archived -> archived = true
        ForceProperty.Unarchived -> archived = false
        ForceProperty.None -> {
          // DO nothing
        }
      }

      val category = predicate.category
      val topic = predicate.topic

      return createNotification(id ?: randomString(), read, seen, archived, category, topic)
    }

    fun createNotification(
      id: String = "123456789",
      read: Boolean = false,
      seen: Boolean = false,
      archived: Boolean = false,
      category: String? = null,
      topic: String? = null
    ): Notification {
      return Notification(
        id = id,
        title = "Testing",
        actionURL = null,
        content = "Lorem ipsum sir dolor amet",
        category = category,
        topic = topic,
        customAttributes = null,
        recipient = null,
        seenAt = if (seen || read) Date() else null,
        sentAt = Date(),
        readAt = if (read) Date() else null,
        archivedAt = if (archived) Date() else null,
      )
    }
  }
}