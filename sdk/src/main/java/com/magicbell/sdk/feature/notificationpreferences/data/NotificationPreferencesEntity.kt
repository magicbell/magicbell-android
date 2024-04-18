package com.magicbell.sdk.feature.notificationpreferences.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class NotificationPreferencesContainerEntity(
  @SerialName("notification_preferences")
  val notificationPreferencesEntity: NotificationPreferencesEntity,
)

@Serializable
internal data class NotificationPreferencesEntity(
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
