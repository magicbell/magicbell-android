package com.magicbell.sdk.common.error

import kotlinx.serialization.Serializable

@Serializable
internal class NetworkErrorEntity(private val errors: List<Error>) {

  @Serializable
  internal data class Error(
    val message: String,
  )

  fun getErrorMessage(default: String): String {
    return if (errors.isNotEmpty()) {
      errors.joinToString(" -- ")
    } else {
      default
    }
  }
}