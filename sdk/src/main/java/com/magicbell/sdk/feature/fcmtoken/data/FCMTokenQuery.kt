package com.magicbell.sdk.feature.fcmtoken.data

import com.mobilejazz.harmony.data.query.Query
import com.magicbell.sdk.common.query.UserQuery

internal class RegisterFCMTokenQuery(val user: UserQuery) : Query()

internal class DeleteFCMTokenQuery(val deviceToken: String, val userQuery: UserQuery) : Query()