package com.magicbell.sdk.feature.userpreferences

/**
 * The user preferences
 */
class UserPreferences(
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
