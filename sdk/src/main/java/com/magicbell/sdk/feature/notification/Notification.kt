package com.magicbell.sdk.feature.notification

import com.magicbell.sdk.common.serialization.DateSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import kotlinx.serialization.json.JsonObject
import java.util.Date

@Serializable
internal data class NotificationEntity(val notification: Notification)

/**
 * A MagicBell notification object
 */
@Serializable
data class Notification(
  val id: String,
  val title: String,
  val content: String? = null,
  @SerialName("action_url")
  val actionURL: String? = null,
  val category: String? = null,
  val topic: String? = null,
  @SerialName("custom_attributes")
  val customAttributes: JsonObject? = null,
  val recipient: Recipient? = null,
  @Serializable(with = DateSerializer::class)
  @SerialName("seenAt")
  @JsonNames("seen_at")
  var seenAt: Date? = null,
  @Serializable(with = DateSerializer::class)
  @SerialName("sentAt")
  @JsonNames("sent_at")
  val sentAt: Date? = null,
  @Serializable(with = DateSerializer::class)
  @SerialName("readAt")
  @JsonNames("read_at")
  var readAt: Date? = null,
  @Serializable(with = DateSerializer::class)
  @SerialName("archivedAt")
  @JsonNames("archived_at")
  var archivedAt: Date? = null,
) {
  val isRead: Boolean
    get() {
      return readAt != null
    }
  val isSeen: Boolean
    get() {
      return seenAt != null
    }
  val isArchived: Boolean
    get() {
      return archivedAt != null
    }
}

@Serializable
data class Recipient(
  val id: String,
  val email: String? = null,
  @SerialName("external_id")
  val externalId: String? = null,
  @SerialName("first_name")
  val firstName: String? = null,
  @SerialName("last_name")
  val lastName: String? = null,
)
