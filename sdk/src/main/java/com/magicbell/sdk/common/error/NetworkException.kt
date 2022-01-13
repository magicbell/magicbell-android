package com.magicbell.sdk.common.error

class NetworkException(val statusCode: Int, message: String? = defaultErrorMessage) : Exception(message) {
  companion object {
    const val defaultErrorMessage = "Network error. Custom message not provided."
  }
}