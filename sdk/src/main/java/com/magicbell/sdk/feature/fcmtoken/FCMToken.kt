package com.magicbell.sdk.feature.fcmtoken

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class FCMTokenEntity(
  @SerialName("fcm")
  val fcmToken: FCMToken,
)

@Serializable
class FCMToken(
  val id: String?,
  @SerialName("device_token")
  val deviceToken: String,
)
