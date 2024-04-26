package com.magicbell.sdk.feature.notificationpreferences

/**
 * The notification preferences object containing all categories
 */
class NotificationPreferences(
  val categories: List<Category>
)

/**
 * The category with its notification channels
 */
class Category(
  val slug: String,
  val label: String,
  val channels: List<Channel>
)

/**
 * The notification channel and its status
 */
class Channel(
  val slug: String,
  val label: String,
  val enabled: Boolean
)