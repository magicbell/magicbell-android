package com.magicbell.sdk.feature.userpreferences.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class UserPreferencesContainerEntity(
  @SerialName("notification_preferences")
  val userPreferencesEntity: UserPreferencesEntity,
)

@Serializable
internal data class UserPreferencesEntity(
  @SerialName("categories")
  val preferences: Map<String, PreferencesEntity>?,
)

@Serializable
internal class PreferencesEntity(
  var email: Boolean,
  @SerialName("in_app")
  var inApp: Boolean,
  @SerialName("mobile_push")
  var mobilePush: Boolean,
  @SerialName("web_push")
  var webPush: Boolean,
)
