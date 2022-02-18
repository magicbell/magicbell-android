package com.magicbell.sdk.feature.store.utils

import com.magicbell.sdk.feature.store.NotificationStoreCountObserver

class CountObserverMock : NotificationStoreCountObserver {

  val totalCountSpy: MutableList<MethodParams.TotalCount> = mutableListOf()
  val totalCountCounter: Int
    get() = totalCountSpy.size

  val unreadCountSpy: MutableList<MethodParams.UnreadCount> = mutableListOf()
  val unreadCountCounter: Int
    get() = unreadCountSpy.size

  val unseenCountSpy: MutableList<MethodParams.UnseenCount> = mutableListOf()
  val unseenCountCounter: Int
    get() = unseenCountSpy.size

  override fun onTotalCountChanged(count: Int) {
    totalCountSpy.add(MethodParams.TotalCount(count))
  }

  override fun onUnreadCountChanged(count: Int) {
    unreadCountSpy.add(MethodParams.UnreadCount(count))
  }

  override fun onUnseenCountChanged(count: Int) {
    unseenCountSpy.add(MethodParams.UnseenCount(count))
  }

  sealed class MethodParams {
    class TotalCount(val count: Int) : MethodParams()
    class UnreadCount(val count: Int) : MethodParams()
    class UnseenCount(val count: Int) : MethodParams()
  }
}