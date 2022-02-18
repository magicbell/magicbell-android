package com.magicbell.sdk.feature.store

import com.magicbell.sdk.feature.notification.Notification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

fun NotificationStore.refresh(
  targetScope: CoroutineScope = CoroutineScope(Dispatchers.Main),
  onSuccess: (List<Notification>) -> Unit,
  onFailure: (Throwable) -> Unit,
) {
  targetScope.launch {
    withContext(coroutineContext) {
      refresh()
    }.onSuccess(onSuccess)
      .onFailure(onFailure)
  }
}

fun NotificationStore.fetch(
  targetScope: CoroutineScope = CoroutineScope(Dispatchers.Main),
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
  targetScope: CoroutineScope = CoroutineScope(Dispatchers.Main),
  onCompletion: (Unit) -> Unit,
  onFailure: (Throwable) -> Unit,
) {
  targetScope.launch {
    withContext(coroutineContext) {
      delete(notification)
    }.onSuccess(onCompletion)
      .onFailure(onFailure)
  }
}

fun NotificationStore.markAsRead(
  notification: Notification,
  targetScope: CoroutineScope = CoroutineScope(Dispatchers.Main),
  onSuccess: (Notification) -> Unit,
  onFailure: (Throwable) -> Unit,
) {
  targetScope.launch {
    withContext(coroutineContext) {
      markAsRead(notification)
    }.onSuccess(onSuccess)
      .onFailure(onFailure)
  }
}

fun NotificationStore.markAsUnread(
  notification: Notification,
  targetScope: CoroutineScope = CoroutineScope(Dispatchers.Main),
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
  targetScope: CoroutineScope = CoroutineScope(Dispatchers.Main),
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
  targetScope: CoroutineScope = CoroutineScope(Dispatchers.Main),
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
  targetScope: CoroutineScope = CoroutineScope(Dispatchers.Main),
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
  targetScope: CoroutineScope = CoroutineScope(Dispatchers.Main),
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
