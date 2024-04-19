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
  val categories: List<CategoryEntity>,
)

@Serializable
internal class CategoryEntity(
  var slug: String,
  var label: String,
  var channels: List<ChannelEntity>,
)

@Serializable
internal class ChannelEntity(
  var slug: String,
  var label: String,
  var enabled: Boolean,
)