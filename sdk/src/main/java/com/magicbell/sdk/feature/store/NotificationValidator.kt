package com.magicbell.sdk.feature.store

import com.magicbell.sdk.feature.notification.Notification

class NotificationValidator(private val storePredicate: StorePredicate) {

  fun validate(notification: Notification): Boolean {
    return validateRead(notification) &&
        validateSeen(notification) &&
        validateArchived(notification) &&
        validateCategory(notification) &&
        validateTopic(notification)
  }

  private fun validateRead(notification: Notification): Boolean {
    return if (storePredicate.read != null && storePredicate.read && notification.readAt != null) {
      true
    } else if (storePredicate.read != null && !storePredicate.read && notification.readAt == null) {
      true
    } else {
      storePredicate.read == null
    }
  }

  private fun validateSeen(notification: Notification): Boolean {
    return if (storePredicate.seen != null && storePredicate.seen && notification.seenAt != null) {
      true
    } else if (storePredicate.seen != null && !storePredicate.seen && notification.seenAt == null) {
      true
    } else {
      storePredicate.seen == null
    }
  }

  private fun validateArchived(notification: Notification): Boolean {
    return if (storePredicate.archived && notification.archivedAt != null) {
      true
    } else {
      !storePredicate.archived && notification.archivedAt == null
    }
  }

  private fun validateCategory(notification: Notification): Boolean {
    return storePredicate.category == null ||
            storePredicate.category == notification.category
  }

  private fun validateTopic(notification: Notification): Boolean {
    return storePredicate.topic == null ||
            storePredicate.topic == notification.topic
  }
}
