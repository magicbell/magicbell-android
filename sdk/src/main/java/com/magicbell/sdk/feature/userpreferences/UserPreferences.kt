package com.magicbell.sdk.feature.userpreferences

class Preferences(
  var email: Boolean,
  var inApp: Boolean,
  var mobilePush: Boolean,
  var webPush: Boolean,
)

class UserPreferences(
  val preferences: Map<String, Preferences>
)