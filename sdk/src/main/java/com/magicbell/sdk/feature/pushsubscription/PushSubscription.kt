package com.magicbell.sdk.feature.pushsubscription

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class PushSubscriptionEntity(
  @SerialName("push_subscription")
  val pushSubscription: PushSubscription,
)

@Serializable
class PushSubscription(
  val id: String?,
  @SerialName("device_token")
  val deviceToken: String,
  val platform: String,
) {
  companion object {
    const val platformAndroid = "android"
  }
}
