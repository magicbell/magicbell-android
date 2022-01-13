package com.magicbell.sdk.feature.pushsubscription.data

import com.harmony.kotlin.data.query.Query
import com.magicbell.sdk.common.query.UserQuery

class RegisterPushSubscriptionQuery(val user: UserQuery) : Query()

class DeletePushSubscriptionQuery(val deviceToken: String, val userQuery: UserQuery) : Query()