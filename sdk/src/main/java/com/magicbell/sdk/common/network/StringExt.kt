package com.magicbell.sdk.common.network

import android.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

internal fun String.hmac(key: String): String {
  val sha256Hmac = Mac.getInstance("HmacSHA256")
  val secretKey = SecretKeySpec(key.toByteArray(), "HmacSHA256")
  sha256Hmac.init(secretKey)
  return Base64.encodeToString(this.toByteArray(), Base64.DEFAULT)
}
