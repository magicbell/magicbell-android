package com.magicbell.sdk.feature.notification.data

import com.harmony.kotlin.data.mapper.Mapper
import com.magicbell.sdk.feature.notification.Notification
import com.magicbell.sdk.feature.notification.NotificationEntity
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

internal class NotificationEntityToNotificationMapper(
  private val serializer: KSerializer<NotificationEntity>,
  private val json: Json,
) : Mapper<String, Notification> {
  override fun map(from: String): Notification {
    val notificationEntity = json.decodeFromString(serializer, from)
    return notificationEntity.notification
  }
}