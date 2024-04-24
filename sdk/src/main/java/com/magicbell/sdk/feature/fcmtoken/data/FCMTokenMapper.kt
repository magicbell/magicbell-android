package com.magicbell.sdk.feature.fcmtoken.data

import com.mobilejazz.harmony.data.mapper.Mapper
import com.magicbell.sdk.feature.fcmtoken.FCMToken
import com.magicbell.sdk.feature.fcmtoken.FCMTokenEntity
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

internal class FCMTokenEntityToFCMTokenMapper(
  private val serializer: KSerializer<FCMTokenEntity>,
  private val json: Json,
) : Mapper<String, FCMToken> {
  override fun map(from: String): FCMToken {
    val fcmTokenEntity = json.decodeFromString(serializer, from)
    return fcmTokenEntity.fcmToken
  }
}

internal class FCMTokenToFCMTokenEntityMapper(
  private val serializer: KSerializer<FCMTokenEntity>,
  private val json: Json,
) : Mapper<FCMToken, String> {
  override fun map(from: FCMToken): String {
    val fcmTokenEntity = FCMTokenEntity(from)
    return json.encodeToString(serializer, fcmTokenEntity)
  }
}
