package com.magicbell.sdk.feature.config

import kotlinx.serialization.Serializable

@Serializable
data class Config(
  private val ws: Ws,
) {
  var channel: String = ws.channel
}

@Serializable
data class Ws(
  val channel: String,
)
