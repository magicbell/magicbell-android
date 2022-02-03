package com.magicbell.sdk_compose

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.magicbell.sdk.feature.notification.Notification
import com.magicbell.sdk.feature.store.NotificationStore
import com.magicbell.sdk.feature.store.NotificationStoreContentObserver
import com.magicbell.sdk.feature.store.NotificationStoreCountObserver
import com.magicbell.sdk.feature.store.refresh


class NotificationStoreViewModel(
  private val notificationStore: NotificationStore,
) : NotificationStoreContentObserver, NotificationStoreCountObserver {

  var totalCount by mutableStateOf(0)
  var unreadCount by mutableStateOf(0)
  var unseenCount by mutableStateOf(0)
  var hasNextPage by mutableStateOf(true)
  var notifications by mutableStateOf(listOf<Notification>())

  init {
    notificationStore.addContentObserver(this)
    notificationStore.addCountObserver(this)
    notificationStore.refresh(onSuccess = {
      onStoreReloaded()
    }, onFailure = {})
  }

  // Content
  override fun onStoreReloaded() {
    totalCount = notificationStore.totalCount
    unreadCount = notificationStore.unreadCount
    unseenCount = notificationStore.unseenCount
    hasNextPage = notificationStore.hasNextPage
    notifications = notificationStore.notifications
  }

  override fun onNotificationsInserted(indexes: List<Int>) {
    val mutableNotifications = notifications.toMutableList()
    indexes.forEach { index ->
      mutableNotifications.add(index, notificationStore[index])
    }
    notifications = mutableNotifications.toList()
  }

  override fun onNotificationsChanged(indexes: List<Int>) {
    notifications = notifications + indexes.map {
      notificationStore[it]
    }
  }

  override fun onNotificationsDeleted(indexes: List<Int>) {
    val mutable = notifications.toMutableList()
    indexes.forEach { index ->
      mutable.removeAt(index)
    }
    notifications = mutable.toList()
  }

  override fun onStoreHasNextPageChanged(hasNextPage: Boolean) {
    this.hasNextPage = hasNextPage
  }

  // Count
  override fun onTotalCountChanged(count: Int) {
    totalCount = count
  }

  override fun onUnreadCountChanged(count: Int) {
    unreadCount = count
  }

  override fun onUnseenCountChanged(count: Int) {
    unseenCount = count
  }
}