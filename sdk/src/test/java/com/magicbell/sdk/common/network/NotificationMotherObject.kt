package com.magicbell.sdk.common.network

import com.magicbell.sdk.feature.notification.Notification
import com.magicbell.sdk.feature.notification.motherobject.ForceProperty
import com.magicbell.sdk.feature.notification.motherobject.anyNotification
import com.magicbell.sdk.feature.store.StorePredicate

internal fun anyNotificationEdgeArray(
  predicate: StorePredicate,
  size: Int,
  forceNotificationProperty: ForceProperty
): List<Notification> {
  return (0 until size).map {
    anyNotification(predicate, it.toString(), forceNotificationProperty)
  }
}

internal fun List<Notification>.totalCount(): Int = this.size

internal fun List<Notification>.unreadCount(): Int = this.filter { !it.isRead }.size

internal fun List<Notification>.unseenCount(): Int = this.filter { !it.isSeen }.size
