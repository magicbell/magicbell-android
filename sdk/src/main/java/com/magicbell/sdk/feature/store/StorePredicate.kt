package com.magicbell.sdk.feature.store

import com.magicbell.sdk.feature.notification.Notification

/**
 * The notification store predicate
 * @param read: The read status. Defaults to null, no filter.
 * @param seen: The seen status. Defaults to null, no filter.
 * @param archived: The archived status. Defaults to false, not archived notifications.
 * @param category: The category. Defaults to null.
 * @param topic: The topic. Defaults to null.
 */
data class StorePredicate(
  val read: Boolean? = null,
  val seen: Boolean? = null,
  val archived: Boolean = false,
  val category: String? = null,
  val topic: String? = null,
)

internal fun StorePredicate.match(notification: Notification): Boolean {
  val validator = NotificationValidator(this)
  return validator.validate(notification)
}
