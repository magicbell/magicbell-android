package com.magicbell.sdk.common.query

import com.mobilejazz.harmony.data.query.KeyQuery

internal class UserQuery private constructor(
  val externalId: String? = null,
  val email: String? = null,
  key: String,
) : KeyQuery(key) {

  companion object {
    fun createExternalId(externalId: String): UserQuery {
      return UserQuery(externalId, null, externalId)
    }

    fun createEmail(email: String): UserQuery {
      return UserQuery(null, email, email)
    }

    fun create(externalId: String, email: String): UserQuery {
      return UserQuery(externalId, email, externalId)
    }
  }
}