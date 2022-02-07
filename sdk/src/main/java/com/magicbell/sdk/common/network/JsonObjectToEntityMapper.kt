package com.magicbell.sdk.common.network

import com.mobilejazz.harmony.data.mapper.Mapper
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

internal class StringToEntityMapper<T>(
  private val serializer: KSerializer<T>,
  private val json: Json,
) : Mapper<String, T> {
  override fun map(from: String): T {
    return json.decodeFromString(serializer, from)
  }
}