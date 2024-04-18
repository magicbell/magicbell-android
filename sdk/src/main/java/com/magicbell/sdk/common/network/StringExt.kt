package com.magicbell.sdk.common.network

import android.util.Base64
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

fun String.hmac(key: String): String {
    try {
        val secretKeySpec = SecretKeySpec(key.toByteArray(charset("UTF-8")), "HmacSHA256")
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(secretKeySpec)
        val rawHmac = mac.doFinal(this.toByteArray(charset("UTF-8")))
        val hmacBase64 = Base64.encodeToString(rawHmac, Base64.NO_WRAP)
        return hmacBase64
    } catch (e: NoSuchAlgorithmException) {
        e.printStackTrace()
    } catch (e: InvalidKeyException) {
        e.printStackTrace()
    }
    return ""
}