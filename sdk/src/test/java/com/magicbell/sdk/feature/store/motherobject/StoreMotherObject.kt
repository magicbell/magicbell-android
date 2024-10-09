package com.magicbell.sdk.feature.store.motherobject

import com.mobilejazz.harmony.common.randomInt
import com.magicbell.sdk.common.network.anyNotificationEdgeArray
import com.magicbell.sdk.common.network.unreadCount
import com.magicbell.sdk.common.network.unseenCount
import com.magicbell.sdk.feature.notification.Notification
import com.magicbell.sdk.feature.notification.motherobject.ForceProperty
import com.magicbell.sdk.feature.store.StorePage
import com.magicbell.sdk.feature.store.StorePredicate

internal fun givenPageStore(predicate: StorePredicate, size: Int, forceProperty: ForceProperty = ForceProperty.None): StorePage {
  val totalPages = (1..10).random()
  val currentPage = (totalPages .. 10).random()
  return StoreMotherObject.createStorePage(
    anyNotificationEdgeArray(predicate, size, forceProperty),
    currentPage,
    totalPages,
  )
}

internal fun anyPageStore(): StorePage {
  val totalPages = (1..10).random()
  val currentPage = (totalPages .. 10).random()
  return StoreMotherObject.createStorePage(
    anyNotificationEdgeArray(predicate = StorePredicate(), randomInt(0, 20), ForceProperty.None),
    currentPage,
    totalPages,
  )
}

internal class StoreMotherObject {
  companion object {
    fun createNoNextPage(notifications: List<Notification>): StorePage {
      return createStorePage(notifications, 1, 1)
    }

    fun createHasNextPage(notifications: List<Notification>): StorePage {
      return createStorePage(notifications, 1, 2)
    }

    fun createAnyNextPage(notifications: List<Notification>): StorePage {
      return createStorePage(notifications, 1, (1..2).random())
    }

    fun createStorePage(
      notifications: List<Notification>,
      currentPage: Int,
      totalPages: Int,
    ): StorePage {
      return StorePage(
        notifications,
        notifications.size,
        notifications.unreadCount(),
        notifications.unseenCount(),
        totalPages,
        notifications.size,
        currentPage,
      )
    }
  }
}
