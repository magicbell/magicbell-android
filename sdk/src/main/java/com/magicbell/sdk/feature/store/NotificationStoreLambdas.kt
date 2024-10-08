package com.magicbell.sdk.feature.store

import com.magicbell.sdk.feature.notification.Notification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

fun NotificationStore.refresh(
  targetScope: CoroutineScope = notificationStoreScope,
  onSuccess: (List<Notification>) -> Unit,
  onFailure: (Throwable) -> Unit,
) {
  targetScope.launch {
    refresh()
      .onSuccess(onSuccess)
      .onFailure(onFailure)
  }
}

fun NotificationStore.fetch(
  targetScope: CoroutineScope = notificationStoreScope,
  onSuccess: (List<Notification>) -> Unit,
  onFailure: (Throwable) -> Unit,
) {
  targetScope.launch {
    withContext(coroutineContext) {
      fetch()
    }.onSuccess(onSuccess)
      .onFailure(onFailure)
  }
}

fun NotificationStore.delete(
  notification: Notification,
  targetScope: CoroutineScope = notificationStoreScope,
  onCompletion: (Unit) -> Unit,
  onFailure: (Throwable) -> Unit,
) {
  targetScope.launch {
    delete(notification)
      .onSuccess(onCompletion)
      .onFailure(onFailure)
  }
}

fun NotificationStore.markAsRead(
  notification: Notification,
  @Suppress("UNUSED_PARAMETER") targetScope: CoroutineScope = notificationStoreScope,
  onSuccess: (Notification) -> Unit,
  onFailure: (Throwable) -> Unit,
) {
  runBlocking {
    markAsRead(notification)
      .onSuccess(onSuccess)
      .onFailure(onFailure)
  }
}

fun NotificationStore.markAsUnread(
  notification: Notification,
  targetScope: CoroutineScope = notificationStoreScope,
  onSuccess: (Notification) -> Unit,
  onFailure: (Throwable) -> Unit,
) {
  targetScope.launch {
    withContext(coroutineContext) {
      markAsUnread(notification)
    }.onSuccess(onSuccess)
      .onFailure(onFailure)
  }
}

fun NotificationStore.archive(
  notification: Notification,
  targetScope: CoroutineScope = notificationStoreScope,
  onSuccess: (Notification) -> Unit,
  onFailure: (Throwable) -> Unit,
) {
  targetScope.launch {
    withContext(coroutineContext) {
      archive(notification)
    }.onSuccess(onSuccess)
      .onFailure(onFailure)
  }
}

fun NotificationStore.unarchive(
  notification: Notification,
  targetScope: CoroutineScope = notificationStoreScope,
  onSuccess: (Notification) -> Unit,
  onFailure: (Throwable) -> Unit,
) {
  targetScope.launch {
    withContext(coroutineContext) {
      unarchive(notification)
    }.onSuccess(onSuccess)
      .onFailure(onFailure)
  }
}

fun NotificationStore.markAllNotificationAsRead(
  targetScope: CoroutineScope = notificationStoreScope,
  onSuccess: (Unit) -> Unit,
  onFailure: (Throwable) -> Unit,
) {
  targetScope.launch {
    withContext(coroutineContext) {
      markAllNotificationAsRead()
    }.onSuccess(onSuccess)
      .onFailure(onFailure)
  }
}

fun NotificationStore.markAllNotificationAsSeen(
  targetScope: CoroutineScope = notificationStoreScope,
  onSuccess: (Unit) -> Unit,
  onFailure: (Throwable) -> Unit,
) {
  targetScope.launch {
    withContext(coroutineContext) {
      markAllNotificationAsSeen()
    }.onSuccess(onSuccess)
      .onFailure(onFailure)
  }
}
