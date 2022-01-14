package com.magicbell.sdk.feature.pushsubscription.data

import com.harmony.kotlin.data.query.Query
import com.magicbell.sdk.common.query.UserQuery

internal class RegisterPushSubscriptionQuery(val user: UserQuery) : Query()

internal class DeletePushSubscriptionQuery(val deviceToken: String, val userQuery: UserQuery) : Query()