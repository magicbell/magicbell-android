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

  fun validateRead(notification: Notification): Boolean {
    return if (storePredicate.read != null && storePredicate.read && notification.readAt != null) {
      true
    } else if (storePredicate.read != null && !storePredicate.read && notification.readAt == null) {
      true
    } else {
      storePredicate.read == null
    }
  }

  fun validateSeen(notification: Notification): Boolean {
    return if (storePredicate.seen != null && storePredicate.seen && notification.seenAt != null) {
      true
    } else if (storePredicate.seen != null && !storePredicate.seen && notification.seenAt == null) {
      true
    } else {
      storePredicate.seen == null
    }
  }

  fun validateArchived(notification: Notification): Boolean {
    return if (storePredicate.archived && notification.archivedAt != null) {
      true
    } else {
      !storePredicate.archived && notification.archivedAt == null
    }
  }

  fun validateCategory(notification: Notification): Boolean {
    return if (storePredicate.categories.isEmpty()) {
      true
    } else if (notification.category != null) {
      storePredicate.categories.contains(notification.category)
    } else {
      false
    }
  }

  fun validateTopic(notification: Notification): Boolean {
    return if (storePredicate.topics.isEmpty()) {
      true
    } else if (notification.topic != null) {
      storePredicate.topics.contains(notification.topic)
    } else {
      false
    }
  }
}
