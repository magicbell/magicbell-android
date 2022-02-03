package com.magicbell.sdk.feature.realtime

import com.magicbell.sdk.feature.config.Config

internal interface StoreRealTime {
  fun startListening(config: Config)
  fun stopListening()
  fun addObserver(observer: StoreRealTimeObserver)
  fun removeObserver(observer: StoreRealTimeObserver)
}

internal enum class StoreRealTimeStatus {
  CONNECTING, CONNECTED, DISCONNECTED
}

internal interface StoreRealTimeObserver {
  fun notifyNewNotification(id: String)
  fun notifyDeleteNotification(id: String)
  fun notifyNotificationChange(id: String, change: StoreRealTimeNotificationChange)
  fun notifyAllNotificationRead()
  fun notifyAllNotificationSeen()
  fun notifyReloadStore()
}

internal enum class StoreRealTimeNotificationChange {
  READ, UNREAD, ARCHIVED
}
