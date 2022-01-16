package com.magicbell.sdk.common.serialization

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

@Serializer(forClass = Date::class)
internal class DateSerializer : KSerializer<Date> {
  private val df: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())

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