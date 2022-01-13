package com.magicbell.sdk.feature.notification

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonNames
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
  val seenAt: Date?,
  @Serializable(with = DateSerializer::class)
  @SerialName("sentAt")
  @JsonNames("sent_at")
  val sentAt: Date?,
  @Serializable(with = DateSerializer::class)
  @SerialName("readAt")
  @JsonNames("read_at")
  val readAt: Date?,
  @Serializable(with = DateSerializer::class)
  @SerialName("archivedAt")
  @JsonNames("archived_at")
  val archiveAt: Date?,
) {
  var isRead: Boolean = readAt != null
  var isSeen: Boolean = seenAt != null
  var isArchived: Boolean = archiveAt != null
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

@Serializer(forClass = Date::class)
object DateSerializer : KSerializer<Date> {
  private val df: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSZ", Locale.getDefault())

  override fun deserialize(decoder: Decoder): Date {
    return try {
      Date(decoder.decodeLong() * 1000)
    } catch (e: Exception) {
      df.parse(decoder.decodeString())
    }
  }

  override fun serialize(encoder: Encoder, value: Date) {
    val dateString = df.format(value)
    encoder.encodeString(dateString)
  }

  override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Date", PrimitiveKind.LONG)
}
