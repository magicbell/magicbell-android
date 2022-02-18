package com.magicbell.sdk.common.network

import com.magicbell.sdk.common.network.graphql.Edge
import com.magicbell.sdk.feature.notification.Notification
import com.magicbell.sdk.feature.notification.motherobject.ForceProperty
import com.magicbell.sdk.feature.notification.motherobject.anyNotification
import com.magicbell.sdk.feature.store.StorePredicate

sealed class AnyCursor(val value: String) {
  object ANY : AnyCursor("any")
  object FIRST_PAGE_CURSOR : AnyCursor("first-cursor")
  object SECOND_PAGE_CURSOR : AnyCursor("second-cursor")
}

internal fun anyNotificationEdgeArray(
  predicate: StorePredicate,
  size: Int,
  forceNotificationProperty: ForceProperty
): List<Edge<Notification>> {
  return (0 until size).map {
    anyNotificationEdge(predicate, it.toString(), forceNotificationProperty)
  }
}

internal fun anyNotificationEdge(predicate: StorePredicate, id: String?, forceProperty: ForceProperty): Edge<Notification> {
  return Edge<Notification>(AnyCursor.ANY.value, anyNotification(predicate, id, forceProperty))
}

internal fun Edge<Notification>.create(cursor: String, notification: Notification): Edge<Notification> {
  return Edge(cursor, notification)
}

internal fun List<Edge<Notification>>.totalCount(): Int = this.size

internal fun List<Edge<Notification>>.unreadCount(): Int = this.filter { !it.node.isRead }.size

internal fun List<Edge<Notification>>.unseenCount(): Int = this.filter { !it.node.isSeen }.size
