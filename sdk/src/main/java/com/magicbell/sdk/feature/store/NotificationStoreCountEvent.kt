package com.magicbell.sdk.feature.store

sealed class NotificationStoreCountEvent {
  /**
   * Notifies the store did change the total count value
   */
  class TotalCountChanged(val count: Int) : NotificationStoreCountEvent()

  /**
   * Notifies the store did change the unread count value
   */
  class UnreadCountChanged(val count: Int) : NotificationStoreCountEvent()

  /**
   * Notifies the store did change the unseen count value
   */
  class UnseenCountChanged(val count: Int) : NotificationStoreCountEvent()
}