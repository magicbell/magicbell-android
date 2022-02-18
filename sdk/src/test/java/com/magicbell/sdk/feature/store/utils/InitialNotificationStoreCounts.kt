package com.magicbell.sdk.feature.store.utils

import com.magicbell.sdk.feature.store.NotificationStore

internal class InitialNotificationStoreCounts(notificationStore: NotificationStore) {
  val totalCount = notificationStore.totalCount
  val unreadCount = notificationStore.unreadCount
  val unseenCount = notificationStore.unseenCount
}