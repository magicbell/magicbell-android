package com.magicbell.sdk.feature.notification

import com.magicbell.sdk.common.serialization.DateSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import java.util.Date

@Serializable
internal data class NotificationEntity(val notification: Notification)

@Serializable
data class Notification(
  val id: String,
  val title: String,
  val content: String?,
  @SerialName("action_url")
  val actionURL: String?,
  val category: String?,
  val topic: String?,
//  @Serializable(with = JsonObject.serializer())
//  @SerialName("custom_attributes")
//  val customAttributes: Map<String, Any>?,
  val recipient: Recipient?,
  @Serializable(with = DateSerializer::class)
  @SerialName("seenAt")
  @JsonNames("seen_at")
  var seenAt: Date?,
  @Serializable(with = DateSerializer::class)
  @SerialName("sentAt")
  @JsonNames("sent_at")
  val sentAt: Date?,
  @Serializable(with = DateSerializer::class)
  @SerialName("readAt")
  @JsonNames("read_at")
  var readAt: Date?,
  @Serializable(with = DateSerializer::class)
  @SerialName("archivedAt")
  @JsonNames("archived_at")
  var archivedAt: Date?,
) {
  var isRead: Boolean = readAt != null
  var isSeen: Boolean = seenAt != null
  var isArchived: Boolean = archivedAt != null
}

@Serializable
data class Recipient(
  val id: String,
  val email: String?,
  @SerialName("external_id")
  val externalId: String?,
  @SerialName("first_name")
  val firstName: String?,
  @SerialName("last_name")
  val lastName: String?,
)
