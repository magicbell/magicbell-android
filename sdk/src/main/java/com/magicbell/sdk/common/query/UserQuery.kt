package com.magicbell.sdk.common.query

import com.mobilejazz.harmony.data.query.KeyQuery

internal class UserQuery private constructor(
  val externalId: String? = null,
  val email: String? = null,
  val hmac: String? = null,
  key: String,
) : KeyQuery(key) {

  companion object {
    fun createExternalId(externalId: String): UserQuery {
      return UserQuery(externalId, null, null, externalId)
    }

    fun createExternalIdHmac(externalId: String, hmac: String): UserQuery {
      return UserQuery(externalId, null, hmac, externalId)
    }

    fun createEmail(email: String): UserQuery {
      return UserQuery(null, email, null, email)
    }

    fun createEmailHmac(email: String, hmac: String): UserQuery {
      return UserQuery(null, email, hmac, email)
    }

    fun create(externalId: String, email: String): UserQuery {
      return UserQuery(externalId, email, null, externalId)
    }

    fun createHmac(externalId: String, email: String, hmac: String): UserQuery {
      return UserQuery(externalId, email, hmac, externalId)
    }
  }
}