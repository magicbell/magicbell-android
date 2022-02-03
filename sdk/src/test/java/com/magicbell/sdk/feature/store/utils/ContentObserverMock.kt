package com.magicbell.sdk.feature.store.utils

import com.magicbell.sdk.feature.store.NotificationStoreContentObserver

class ContentObserverMock : NotificationStoreContentObserver {

  val reloadStoreSpy: MutableList<MethodParams.ReloadStore> = mutableListOf()
  val reloadStoreCounter: Int
    get() = reloadStoreSpy.size

  val didInsertSpy: MutableList<MethodParams.DidInsert> = mutableListOf()
  val didInsertCounter: Int
    get() = didInsertSpy.size

  val didChangeSpy: MutableList<MethodParams.DidChange> = mutableListOf()
  val didChangeCounter: Int
    get() = didChangeSpy.size

  val didDeleteSpy: MutableList<MethodParams.DidDelete> = mutableListOf()
  val didDeleteCounter: Int
    get() = didDeleteSpy.size

  val didChangeHastNextPageSpy: MutableList<MethodParams.DidChangeHasNextPage> = mutableListOf()
  val didChangeHastNextPageCounter: Int
    get() = didChangeHastNextPageSpy.size

  override fun onStoreReloaded() {
    reloadStoreSpy.add(MethodParams.ReloadStore)
  }

  override fun onNotificationsInserted(indexes: List<Int>) {
    didInsertSpy.add(MethodParams.DidInsert(indexes))
  }

  override fun onNotificationsChanged(indexes: List<Int>) {
    didChangeSpy.add(MethodParams.DidChange(indexes))
  }

  override fun onNotificationsDeleted(indexes: List<Int>) {
    didDeleteSpy.add(MethodParams.DidDelete(indexes))
  }

  override fun onStoreHasNextPageChanged(hasNextPage: Boolean) {
    didChangeHastNextPageSpy.add(MethodParams.DidChangeHasNextPage(hasNextPage))
  }

  sealed class MethodParams {
    object ReloadStore : MethodParams()
    class DidInsert(val indexes: List<Int>) : MethodParams()
    class DidChange(val indexes: List<Int>) : MethodParams()
    class DidDelete(val indexes: List<Int>) : MethodParams()
    class DidChangeHasNextPage(val hasNextPage: Boolean) : MethodParams()
  }
}