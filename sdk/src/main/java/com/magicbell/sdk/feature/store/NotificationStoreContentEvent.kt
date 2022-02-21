package com.magicbell.sdk.feature.store

sealed class NotificationStoreContentEvent {
  /**
   * Notifies the store did fully reload
   */
  object Reloaded : NotificationStoreContentEvent()

  /**
   * Notifies the store did insert new notifications at certain indexes.
   */
  class Inserted(val indexes: List<Int>) : NotificationStoreContentEvent()

  /**
   * Notifies the store did change notifications at certain indexes.
   */
  class Changed(val indexes: List<Int>) : NotificationStoreContentEvent()

  /**
   * Notifies the store did delete notifications at certain indexes.
   */
  class Deleted(val indexes: List<Int>) : NotificationStoreContentEvent()

  /**
   * Notifies if the store has more pages to load
   */
  class HasNextPageChanged(val hasNextPage: Boolean) : NotificationStoreContentEvent()
}
