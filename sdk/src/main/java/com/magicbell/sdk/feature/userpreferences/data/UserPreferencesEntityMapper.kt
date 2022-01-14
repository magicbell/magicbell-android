package com.magicbell.sdk.feature.userpreferences.data

import com.harmony.kotlin.data.mapper.Mapper
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

internal class UserPreferencesContainerEntityToUserPreferencesEntityMapper(
  private val serializer: KSerializer<UserPreferencesContainerEntity>,
  private val json: Json,
) : Mapper<String, UserPreferencesEntity> {
  override fun map(from: String): UserPreferencesEntity {
    val userPreferencesContainerEntity = json.decodeFromString(serializer, from)
    return userPreferencesContainerEntity.userPreferencesEntity
  }
}

internal class UserPreferencesEntityToUserPreferencesContainerEntityMapper(
  private val serializer: KSerializer<UserPreferencesContainerEntity>,
  private val json: Json,
) : Mapper<UserPreferencesEntity, String> {
  override fun map(from: UserPreferencesEntity): String {
    val userPreferencesContainerEntity = UserPreferencesContainerEntity(from)
    return json.encodeToString(serializer, userPreferencesContainerEntity)
  }
}