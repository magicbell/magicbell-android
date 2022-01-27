package com.magicbell.sdk.feature.store


/**
 * The Store content Observer
 */
interface NotificationStoreContentObserver {
  /**
   * Notifies the store did fully reload
   */
  fun onStoreReloaded()

  /**
   * Notifies the store did insert new notifications at certain indexes.
   */
  fun onNotificationsInserted(indexes: List<Int>)

  /**
   * Notifies the store did change notifications at certain indexes.
   */
  fun onNotificationsChanged(indexes: List<Int>)

  /**
   * Notifies the store did delete notifications at certain indexes.
   */
  fun onNotificationsDeleted(indexes: List<Int>)

  /**
   * Notifies if the store has more pages to load
   */
  fun onStoreHasNextPageChanged(hasNextPage: Boolean)
}

/**
 * The Store count Observer
 */
interface NotificationStoreCountObserver {
  /**
   * Notifies the store did change the total count value
   */
  fun onTotalCountChanged(count: Int)

  /**
   * Notifies the store did change the unread count value
   */
  fun onUnreadCountChanged(count: Int)

  /**
   * Notifies the store did change the unseen count value
   */
  fun onUnseenCountChanged(count: Int)
}