package com.magicbell.sdk.feature.pushsubscription.data

import com.mobilejazz.harmony.data.mapper.Mapper
import com.magicbell.sdk.feature.pushsubscription.PushSubscription
import com.magicbell.sdk.feature.pushsubscription.PushSubscriptionEntity
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

internal class PushSubscriptionEntityToPushSubscriptionMapper(
  private val serializer: KSerializer<PushSubscriptionEntity>,
  private val json: Json,
) : Mapper<String, PushSubscription> {
  override fun map(from: String): PushSubscription {
    val pushSubscriptionEntity = json.decodeFromString(serializer, from)
    return pushSubscriptionEntity.pushSubscription
  }
}

internal class PushSubscriptionToPushSubscriptionEntityMapper(
  private val serializer: KSerializer<PushSubscriptionEntity>,
  private val json: Json,
) : Mapper<PushSubscription, String> {
  override fun map(from: PushSubscription): String {
    val pushSubscriptionEntity = PushSubscriptionEntity(from)
    return json.encodeToString(serializer, pushSubscriptionEntity)
  }
}
