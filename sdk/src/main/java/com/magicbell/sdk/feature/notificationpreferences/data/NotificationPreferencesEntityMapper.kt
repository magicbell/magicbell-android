package com.magicbell.sdk.feature.notificationpreferences.data

import com.mobilejazz.harmony.data.mapper.Mapper
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

internal class NotificationPreferencesContainerEntityToNotificationPreferencesEntityMapper(
  private val serializer: KSerializer<NotificationPreferencesContainerEntity>,
  private val json: Json,
) : Mapper<String, NotificationPreferencesEntity> {
  override fun map(from: String): NotificationPreferencesEntity {
    val notificationPreferencesContainerEntity = json.decodeFromString(serializer, from)
    return notificationPreferencesContainerEntity.notificationPreferencesEntity
  }
}

internal class NotificationPreferencesEntityToNotificationPreferencesContainerEntityMapper(
  private val serializer: KSerializer<NotificationPreferencesContainerEntity>,
  private val json: Json,
) : Mapper<NotificationPreferencesEntity, String> {
  override fun map(from: NotificationPreferencesEntity): String {
    val notificationPreferencesContainerEntity = NotificationPreferencesContainerEntity(from)
    return json.encodeToString(serializer, notificationPreferencesContainerEntity)
  }
}