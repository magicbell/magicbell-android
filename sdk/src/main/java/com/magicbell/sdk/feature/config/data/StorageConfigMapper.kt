package com.magicbell.sdk.feature.config.data

import com.harmony.kotlin.data.mapper.Mapper
import com.magicbell.sdk.feature.config.Config
import com.magicbell.sdk.feature.config.Ws

internal class StringToConfigMapper : Mapper<String, Config> {
  override fun map(from: String): Config {
    return Config(Ws(from))
  }
}

internal class ConfigToStringMapper : Mapper<Config, String> {
  override fun map(from: Config): String {
    return from.channel
  }
}