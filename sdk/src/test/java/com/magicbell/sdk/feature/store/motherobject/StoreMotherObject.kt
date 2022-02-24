package com.magicbell.sdk.feature.store.motherobject

import com.mobilejazz.harmony.common.randomInt
import com.magicbell.sdk.common.network.anyNotificationEdgeArray
import com.magicbell.sdk.common.network.anyPageInfo
import com.magicbell.sdk.common.network.graphql.Edge
import com.magicbell.sdk.common.network.graphql.PageInfo
import com.magicbell.sdk.feature.notification.Notification
import com.magicbell.sdk.feature.notification.motherobject.ForceProperty
import com.magicbell.sdk.feature.store.StorePage
import com.magicbell.sdk.feature.store.StorePredicate

internal fun givenPageStore(predicate: StorePredicate, size: Int, forceProperty: ForceProperty = ForceProperty.None): StorePage {
  return StoreMotherObject.createStorePage(
    anyNotificationEdgeArray(predicate, size, forceProperty),
    anyPageInfo()
  )
}

internal fun anyPageStore(): StorePage {
  return StoreMotherObject.createStorePage(
    anyNotificationEdgeArray(predicate = StorePredicate(), randomInt(0, 20), ForceProperty.None),
    anyPageInfo()
  )
}

internal class StoreMotherObject {
  companion object {
    fun createStorePage(
      edges: List<Edge<Notification>>,
      pageInfo: PageInfo
    ): StorePage {
      return StorePage(
        edges,
        pageInfo,
        edges.size,
        edges.filter { !it.node.isRead }.size,
        edges.filter { !it.node.isSeen }.size
      )
    }
  }
}
