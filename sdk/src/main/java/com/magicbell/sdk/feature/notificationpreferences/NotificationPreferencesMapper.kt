package com.magicbell.sdk.feature.notificationpreferences

import com.mobilejazz.harmony.data.mapper.Mapper
import com.magicbell.sdk.feature.notificationpreferences.data.PreferencesEntity
import com.magicbell.sdk.feature.notificationpreferences.data.NotificationPreferencesEntity

internal class UserPreferencesEntityToUserPreferencesMapper : Mapper<NotificationPreferencesEntity, NotificationPreferences> {
  override fun map(from: NotificationPreferencesEntity): NotificationPreferences {
    return if (from.preferences != null) {
      val preferences = from.preferences.map {
        val preferencesEntity = it.value
        it.key to Preferences(preferencesEntity.email, preferencesEntity.inApp, preferencesEntity.mobilePush, preferencesEntity.webPush)
      }.toMap()
      NotificationPreferences(preferences)
    } else {
      NotificationPreferences(mapOf())
    }
  }
}

internal class UserPreferencesToUserPreferencesEntityMapper : Mapper<NotificationPreferences, NotificationPreferencesEntity> {
  override fun map(from: NotificationPreferences): NotificationPreferencesEntity {
    val preferences = from.preferences.map {
      val preferencesEntity = it.value
      it.key to PreferencesEntity(preferencesEntity.email, preferencesEntity.inApp, preferencesEntity.mobilePush, preferencesEntity.webPush)
    }.toMap()
    return NotificationPreferencesEntity(preferences)
  }
}