package com.magicbell.sdk.common.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = Date::class)
internal class DateSerializer : KSerializer<Date?> {
  private val df: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())

  override fun deserialize(decoder: Decoder): Date? {
    try {
      return Date(decoder.decodeLong() * 1000)
    } catch (e: Exception) {
      return try {
        df.parse(decoder.decodeString())
      } catch (e: Exception) {
        return decoder.decodeNull()
      }
    }
  }

  override fun serialize(encoder: Encoder, value: Date?) {
    val dateString = value?.let { df.format(it) }
    if (dateString != null) {
      encoder.encodeString(dateString)
    } else {
      encoder.encodeNull()
    }
  }

  override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Date", PrimitiveKind.LONG)
}