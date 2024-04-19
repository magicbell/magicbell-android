package com.magicbell.sdk.feature.notificationpreferences

import com.magicbell.sdk.feature.notificationpreferences.data.CategoryEntity
import com.magicbell.sdk.feature.notificationpreferences.data.ChannelEntity
import com.mobilejazz.harmony.data.mapper.Mapper
import com.magicbell.sdk.feature.notificationpreferences.data.NotificationPreferencesEntity


internal class UserPreferencesEntityToUserPreferencesMapper : Mapper<NotificationPreferencesEntity, NotificationPreferences> {
  override fun map(from: NotificationPreferencesEntity): NotificationPreferences {
    return NotificationPreferences(from.categories.map { cat ->
      Category(cat.slug, cat.label, cat.channels.map { ch ->
        Channel(ch.slug, ch.label, ch.enabled)
      })
    })
  }
}

internal class UserPreferencesToUserPreferencesEntityMapper : Mapper<NotificationPreferences, NotificationPreferencesEntity> {
  override fun map(from: NotificationPreferences): NotificationPreferencesEntity {
    return NotificationPreferencesEntity(from.categories.map { cat ->
      CategoryEntity(cat.slug, cat.label, cat.channels.map { ch ->
        ChannelEntity(ch.slug, ch.label, ch.enabled)
      })
    })
  }
}