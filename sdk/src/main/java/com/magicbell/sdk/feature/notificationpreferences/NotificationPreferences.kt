package com.magicbell.sdk.feature.notificationpreferences

/**
 * The notification preferences
 */
class NotificationPreferences(
  val preferences: Map<String, Preferences>
)

/**
 * The preferences of notifications
 */
class Preferences(
  var email: Boolean,
  var inApp: Boolean,
  var mobilePush: Boolean,
  var webPush: Boolean,
)
