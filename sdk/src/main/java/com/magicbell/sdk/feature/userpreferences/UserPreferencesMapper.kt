package com.magicbell.sdk.feature.userpreferences

import com.mobilejazz.harmony.data.mapper.Mapper
import com.magicbell.sdk.feature.userpreferences.data.PreferencesEntity
import com.magicbell.sdk.feature.userpreferences.data.UserPreferencesEntity

internal class UserPreferencesEntityToUserPreferencesMapper : Mapper<UserPreferencesEntity, UserPreferences> {
  override fun map(from: UserPreferencesEntity): UserPreferences {
    return if (from.preferences != null) {
      val preferences = from.preferences.map {
        val preferencesEntity = it.value
        it.key to Preferences(preferencesEntity.email, preferencesEntity.inApp, preferencesEntity.mobilePush, preferencesEntity.webPush)
      }.toMap()
      UserPreferences(preferences)
    } else {
      UserPreferences(mapOf())
    }
  }
}

internal class UserPreferencesToUserPreferencesEntityMapper : Mapper<UserPreferences, UserPreferencesEntity> {
  override fun map(from: UserPreferences): UserPreferencesEntity {
    val preferences = from.preferences.map {
      val preferencesEntity = it.value
      it.key to PreferencesEntity(preferencesEntity.email, preferencesEntity.inApp, preferencesEntity.mobilePush, preferencesEntity.webPush)
    }.toMap()
    return UserPreferencesEntity(preferences)
  }
}