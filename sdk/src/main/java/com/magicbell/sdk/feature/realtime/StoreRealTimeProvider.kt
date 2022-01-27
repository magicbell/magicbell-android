package com.magicbell.sdk.feature.realtime

import com.magicbell.sdk.common.environment.Environment
import com.magicbell.sdk.common.query.UserQuery

internal interface StoreRealTimeComponent {
  fun createStoreRealTime(userQuery: UserQuery): StoreRealTime
}

internal class DefaultStoreRealTimeModule(private val environment: Environment) : StoreRealTimeComponent {
  override fun createStoreRealTime(userQuery: UserQuery): StoreRealTime {
    return AblyConnector(environment, userQuery)
  }
}