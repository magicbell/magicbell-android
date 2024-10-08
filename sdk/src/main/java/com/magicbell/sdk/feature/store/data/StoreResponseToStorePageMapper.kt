package com.magicbell.sdk.feature.store.data

import com.magicbell.sdk.feature.store.StorePage
import com.mobilejazz.harmony.data.mapper.Mapper
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

internal class StoreResponseToStorePageMapper(
  private val storePageSerializer: KSerializer<StorePage>,
  private val json: Json,
) : Mapper<String, StorePage> {
  override fun map(from: String): StorePage {
    val storePage = json.decodeFromString(storePageSerializer, from)
    return storePage
  }
}
