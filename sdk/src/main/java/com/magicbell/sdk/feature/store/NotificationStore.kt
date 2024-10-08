package com.magicbell.sdk.feature.store

import androidx.annotation.VisibleForTesting
import com.magicbell.sdk.common.error.MagicBellError
import com.magicbell.sdk.common.network.graphql.CursorPredicate
import com.magicbell.sdk.common.network.graphql.CursorPredicate.Cursor.Next
import com.magicbell.sdk.common.query.UserQuery
import com.magicbell.sdk.common.threading.MainThread
import com.magicbell.sdk.feature.notification.Notification
import com.magicbell.sdk.feature.notification.data.NotificationActionQuery
import com.magicbell.sdk.feature.notification.data.NotificationActionQuery.Action.ARCHIVE
import com.magicbell.sdk.feature.notification.data.NotificationActionQuery.Action.MARK_ALL_AS_READ
import com.magicbell.sdk.feature.notification.data.NotificationActionQuery.Action.MARK_ALL_AS_SEEN
import com.magicbell.sdk.feature.notification.data.NotificationActionQuery.Action.MARK_AS_READ
import com.magicbell.sdk.feature.notification.data.NotificationActionQuery.Action.MARK_AS_UNREAD
import com.magicbell.sdk.feature.notification.data.NotificationActionQuery.Action.UNARCHIVE
import com.magicbell.sdk.feature.notification.interactor.ActionNotificationInteractor
import com.magicbell.sdk.feature.notification.interactor.DeleteNotificationInteractor
import com.magicbell.sdk.feature.realtime.StoreRealTimeNotificationChange
import com.magicbell.sdk.feature.realtime.StoreRealTimeObserver
import com.magicbell.sdk.feature.store.interactor.FetchStorePageInteractor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.WeakHashMap
import kotlin.coroutines.CoroutineContext

/**
 * The NotificationStore class represents a collection of MagicBell notifications.
 */
class NotificationStore internal constructor(
  val predicate: StorePredicate,
  private val coroutineContext: CoroutineContext,
  val notificationStoreScope: CoroutineScope,
  private val mainThread: MainThread,
  private val userQuery: UserQuery,
  private val fetchStorePageInteractor: FetchStorePageInteractor,
  private val actionNotificationInteractor: ActionNotificationInteractor,
  private val deleteNotificationInteractor: DeleteNotificationInteractor,
) : List<Notification> {

  internal val realTimeObserver = object : StoreRealTimeObserver {
    override fun notifyNewNotification(id: String) {
      refreshAndNotifyObservers()
    }

    override fun notifyDeleteNotification(id: String) {
      val notificationIndex = notifications.indexOfFirst { it.id == id }
      if (notificationIndex != -1) {
        updateCountersWhenDelete(notifications[notificationIndex], predicate)
        (notifications as MutableList).removeAt(notificationIndex)
        notifyObserversDeleted(listOf(notificationIndex))
      }
    }

    override fun notifyNotificationChange(id: String, change: StoreRealTimeNotificationChange) {
      val notificationIndex = notifications.indexOfFirst { it.id == id }
      if (notificationIndex != -1) {
        val notification = notifications[notificationIndex]
        when (change) {
          StoreRealTimeNotificationChange.READ -> markNotificationAsRead(notification, predicate)
          StoreRealTimeNotificationChange.UNREAD -> markNotificationAsUnread(notification, predicate)
          StoreRealTimeNotificationChange.ARCHIVED -> archiveNotification(notification, predicate)
        }
        if (predicate.match(notification)) {
          (notifications as MutableList)[notificationIndex] = notification
          notifyObserversChanged(listOf(notificationIndex))
        } else {
          (notifications as MutableList).removeAt(notificationIndex)
          notifyObserversDeleted(listOf(notificationIndex))
        }
      } else {
        refreshAndNotifyObservers()
      }
    }

    override fun notifyAllNotificationRead() {
      if (predicate.read == null || predicate.read == true) {
        refreshAndNotifyObservers()
      } else {
        clear(true)
      }
    }

    override fun notifyAllNotificationSeen() {
      if (predicate.seen == null || predicate.seen == true) {
        refreshAndNotifyObservers()
      } else {
        clear(true)
      }
    }

    override fun notifyReloadStore() {
      refreshAndNotifyObservers()
    }
  }

  private fun refreshAndNotifyObservers() {
    notificationStoreScope.launch {
      refresh()
    }
  }

  private val pageSize = 20

  var totalCount: Int = 0
    private set
  var unreadCount: Int = 0
    private set
  var unseenCount: Int = 0
    private set
  var hasNextPage: Boolean = true
    private set

  private var nextPageCursor: String? = null

  private val mutableContentFlow = MutableSharedFlow<NotificationStoreContentEvent>()

  /**
   * A flow that observes all changes in the notification store
   */
  val contentFlow = mutableContentFlow.asSharedFlow()

  private val mutableCountFlow = MutableSharedFlow<NotificationStoreCountEvent>()

  /**
   * A flow that observes all changes in the notification store
   */
  val countFlow = mutableCountFlow.asSharedFlow()

  @VisibleForTesting
  private val contentObservers = WeakHashMap<NotificationStoreContentObserver, NotificationStoreContentObserver>()

  @VisibleForTesting
  private val countObservers = WeakHashMap<NotificationStoreCountObserver, NotificationStoreCountObserver>()

  private fun setTotalCount(value: Int, notifyObservers: Boolean) {
    val oldValue = totalCount
    totalCount = value
    if (oldValue != totalCount && notifyObservers) {
      notificationStoreScope.launch {
        mutableCountFlow.emit(NotificationStoreCountEvent.TotalCountChanged(totalCount))
      }
      forEachCountObserver { it.onTotalCountChanged(totalCount) }
    }
  }

  private fun setUnreadCount(value: Int, notifyObservers: Boolean) {
    val oldValue = unreadCount
    unreadCount = value
    if (oldValue != unreadCount && notifyObservers) {
      notificationStoreScope.launch {
        mutableCountFlow.emit(NotificationStoreCountEvent.UnreadCountChanged(unreadCount))
      }
      forEachCountObserver { it.onUnreadCountChanged(unreadCount) }
    }
  }

  private fun setUnseenCount(value: Int, notifyObservers: Boolean) {
    val oldValue = unseenCount
    unseenCount = value
    if (oldValue != unseenCount && notifyObservers) {
      notificationStoreScope.launch {
        mutableCountFlow.emit(NotificationStoreCountEvent.UnseenCountChanged(unseenCount))
      }
      forEachCountObserver { it.onUnseenCountChanged(unseenCount) }
    }
  }

  private fun setHasNextPage(value: Boolean) {
    val oldValue = hasNextPage
    hasNextPage = value
    if (oldValue != hasNextPage) {
      notifyObserverHasNextPage(hasNextPage)
    }
  }

  /**
   * Returns a list containing all notifications
   */
  val notifications: List<Notification> = mutableListOf()

  /**
   * Number of notifications loaded in the store
   */
  val count: Int = notifications.count()

  override val size: Int
    get() {
      return notifications.size
    }

  override fun get(index: Int): Notification {
    return notifications[index]
  }

  override fun contains(element: Notification): Boolean {
    return notifications.firstOrNull { it.id == element.id } != null
  }

  override fun containsAll(elements: Collection<Notification>): Boolean = notifications.containsAll(elements)

  override fun isEmpty(): Boolean = notifications.isEmpty()

  override fun iterator(): Iterator<Notification> = notifications.iterator()

  override fun indexOf(element: Notification): Int {
    return notifications.indexOfFirst { it.id == element.id }
  }

  override fun lastIndexOf(element: Notification): Int {
    return notifications.indexOfLast { it.id == element.id }
  }

  override fun listIterator(): ListIterator<Notification> = notifications.listIterator()

  override fun listIterator(index: Int): ListIterator<Notification> = notifications.listIterator(index)

  override fun subList(fromIndex: Int, toIndex: Int): List<Notification> {
    return notifications.subList(fromIndex, toIndex)
  }

  //region Observers
  /**
   * Adds a content observer.
   *
   * @param observer The observer
   */
  fun addContentObserver(observer: NotificationStoreContentObserver) {
    contentObservers[observer] = observer
  }

  /**
   * Removes a content observer.
   *
   * @param observer The observer
   */
  fun removeContentObserver(observer: NotificationStoreContentObserver) {
    contentObservers.remove(observer)
  }

  /**
   * Adds a count observer.
   *
   * @param observer The observer
   */
  fun addCountObserver(observer: NotificationStoreCountObserver) {
    countObservers[observer] = observer
  }

  /**
   * Removes a count observer.
   *
   * @param observer The observer
   */
  fun removeCountObserver(observer: NotificationStoreCountObserver) {
    countObservers.remove(observer)
  }

  private fun forEachContentObserver(action: (NotificationStoreContentObserver) -> Unit) {
    mainThread.post {
      contentObservers.values.forEach { observer ->
        action(observer)
      }
    }
  }

  private fun forEachCountObserver(action: (NotificationStoreCountObserver) -> Unit) {
    mainThread.post {
      countObservers.values.forEach { observer ->
        action(observer)
      }
    }
  }

  private fun notifyObserversReloadStore() {
    notificationStoreScope.launch {
      mutableContentFlow.emit(NotificationStoreContentEvent.Reloaded)
    }
    forEachContentObserver { it.onStoreReloaded() }
  }

  private fun notifyObserversInserted(notificationIndexes: List<Int>) {
    notificationStoreScope.launch {
      mutableContentFlow.emit(NotificationStoreContentEvent.Inserted(notificationIndexes))
    }
    forEachContentObserver { it.onNotificationsInserted(notificationIndexes) }
  }

  private fun notifyObserversChanged(notificationIndexes: List<Int>) {
    notificationStoreScope.launch {
      mutableContentFlow.emit(NotificationStoreContentEvent.Changed(notificationIndexes))
    }
    forEachContentObserver { it.onNotificationsChanged(notificationIndexes) }
  }

  private fun notifyObserversDeleted(notificationIndexes: List<Int>) {
    notificationStoreScope.launch {
      mutableContentFlow.emit(NotificationStoreContentEvent.Deleted(notificationIndexes))
    }
    forEachContentObserver { it.onNotificationsDeleted(notificationIndexes) }
  }

  private fun notifyObserverHasNextPage(hasNextPage: Boolean) {
    notificationStoreScope.launch {
      mutableContentFlow.emit(NotificationStoreContentEvent.HasNextPageChanged(hasNextPage))
    }
    forEachContentObserver { it.onStoreHasNextPageChanged(hasNextPage) }
  }
  //endregion

  /**
   * Clears the store and fetches first page.
   *
   * @return A Result with the list of notifications.
   */
  suspend fun refresh(): Result<List<Notification>> {
    return runCatching {
      withContext(coroutineContext) {
        val cursorPredicate = CursorPredicate(size = pageSize)
        val storePage = fetchStorePageInteractor(predicate, cursorPredicate, userQuery)
        clear(false)
        configurePagination(storePage)
        configureCount(storePage)
        val newNotifications = storePage.notifications
        (notifications as MutableList).addAll(newNotifications)
        notifyObserversReloadStore()
        newNotifications
      }
    }
  }

  /**
   * Fetches the next page of notifications. It can be called multiple times to obtain all pages.
   * This method will notify the observers if changes are made into the store.
   *
   * @return A Result with the list of notifications.
   */
  suspend fun fetch(): Result<List<Notification>> {
    return runCatching {
      withContext(coroutineContext) {
        if (!hasNextPage) {
          return@withContext listOf<Notification>()
        }
        val cursorPredicate: CursorPredicate = nextPageCursor?.let { after ->
          CursorPredicate(Next(after), pageSize)
        } ?: run {
          CursorPredicate(size = pageSize)
        }

        val storePage = fetchStorePageInteractor(predicate, cursorPredicate, userQuery)
        configurePagination(storePage)
        configureCount(storePage)

        val oldCount = notifications.count()
        val newNotifications = storePage.notifications
        (notifications as MutableList).addAll(newNotifications)
        val indexes = oldCount until notifications.size
        notifyObserversInserted(indexes.toList())
        newNotifications
      }
    }
  }

  /**
   * Deletes a notification.
   *
   * @param notification The notification.
   * @return A Result with the error if exists.
   */
  suspend fun delete(notification: Notification): Result<Unit> {
    return runCatching {
      withContext(coroutineContext) {
        deleteNotificationInteractor(notification.id, userQuery)
        val notificationIndex = notifications.indexOfFirst { it.id == notification.id }
        if (notificationIndex != -1) {
          updateCountersWhenDelete(notifications[notificationIndex], predicate)
          (notifications as MutableList).removeAt(notificationIndex)
          notifyObserversDeleted(listOf(notificationIndex))
        }
      }
    }
  }

  /**
   * Marks a notification as read.
   *
   * @param notification The notification.
   * @return A Result with the modified notification.
   */
  suspend fun markAsRead(notification: Notification): Result<Notification> {
    return executeNotificationAction(
      notification,
      MARK_AS_READ,
      modifications = { notification ->
        markNotificationAsRead(notification, predicate)
      })
  }

  /**
   * Marks a notification as unread.
   *
   * @param notification The notification.
   * @return A Result with the modified notification.
   */
  suspend fun markAsUnread(notification: Notification): Result<Notification> {
    return executeNotificationAction(
      notification,
      MARK_AS_UNREAD,
      modifications = { notification ->
        markNotificationAsUnread(notification, predicate)
      })
  }

  /**
   * Marks a notification as archived.
   *
   * @param notification The notification.
   * @return A Result with the modified notification.
   */
  suspend fun archive(notification: Notification): Result<Notification> {
    return executeNotificationAction(
      notification,
      ARCHIVE,
      modifications = { notification ->
        archiveNotification(notification, predicate)
      })
  }

  /**
   * Marks a notification as unarchived.
   *
   * @param notification The notification.
   * @return A Result with the modified notification.
   */
  suspend fun unarchive(notification: Notification): Result<Notification> {
    return executeNotificationAction(
      notification,
      UNARCHIVE,
      modifications = { notification ->
        notification.archivedAt = null
      })
  }

  /**
   * Marks all notifications as read.
   *
   * @return A Result with the error if exists.
   */
  suspend fun markAllNotificationAsRead(): Result<Unit> {
    return executeAllNotificationsAction(
      MARK_ALL_AS_READ,
      modifications = { notification ->
        markNotificationAsRead(notification, predicate)
      })
  }

  /**
   * Marks all notifications as seen.
   *
   * @return A Result with the error if exists.
   */
  suspend fun markAllNotificationAsSeen(): Result<Unit> {
    return executeAllNotificationsAction(
      MARK_ALL_AS_SEEN,
      modifications = { notification ->
        if (notification.seenAt == null) {
          notification.seenAt = Date()
          unseenCount -= 1
        }
      })
  }

  //region Private methods
  //region NotificationActions
  private fun clear(notifyChanges: Boolean) {
    val notificationCount = size
    (notifications as MutableList).clear()
    setTotalCount(0, notifyChanges)
    setUnreadCount(0, notifyChanges)
    setUnseenCount(0, notifyChanges)
    nextPageCursor = null
    setHasNextPage(true)
    if (notifyChanges) {
      val indexes = 0 until notificationCount
      notifyObserversDeleted(indexes.toList())
    }
  }

  private suspend fun executeNotificationAction(
    notification: Notification,
    action: NotificationActionQuery.Action,
    modifications: (Notification) -> Unit,
  ): Result<Notification> {
    return runCatching {
      withContext(coroutineContext) {
        actionNotificationInteractor(action, notification.id, userQuery)
        val notificationIndex = notifications.indexOfFirst { it.id == notification.id }
        if (notificationIndex != -1) {
          modifications(notification)
          notification
        } else {
          throw MagicBellError("Notification not found in Store")
        }
      }
    }
  }

  private suspend fun executeAllNotificationsAction(
    action: NotificationActionQuery.Action,
    modifications: (Notification) -> Unit,
  ): Result<Unit> {
    return runCatching {
      withContext(coroutineContext) {
        actionNotificationInteractor(action, userQuery = userQuery)
        for (i in notifications.indices) {
          modifications(notifications[i])
        }
      }
    }
  }

  private fun configurePagination(storePage: StorePage) {
    // TODO: pagination
//    val pageInfo = storePage.pageInfo
//    nextPageCursor = pageInfo.endCursor
    setHasNextPage(storePage.currentPage < storePage.totalPages)
  }

  private fun configureCount(storePage: StorePage) {
    setTotalCount(storePage.totalCount, true)
    setUnreadCount(storePage.unreadCount, true)
    setUnseenCount(storePage.unseenCount, true)
  }

  private fun markNotificationAsRead(notification: Notification, storePredicate: StorePredicate) {
    if (notification.seenAt == null) {
      setUnseenCount(unseenCount - 1, true)
    }

    if (notification.readAt == null) {
      setUnreadCount(unreadCount - 1, true)

      storePredicate.read?.also { isRead ->
        if (isRead) {
          setTotalCount(totalCount - 1, true)
        } else {
          setTotalCount(totalCount - 1, true)
        }
      }
    }

    val now = Date()
    notification.readAt = now
    notification.seenAt = now
  }

  private fun markNotificationAsUnread(notification: Notification, storePredicate: StorePredicate) {
    if (notification.readAt != null) {
      storePredicate.read?.also {
        if (it) {
          setTotalCount(totalCount - 1, true)
          setUnreadCount(0, true)
        } else {
          setTotalCount(totalCount + 1, true)
          setUnreadCount(unreadCount + 1, true)
        }
      } ?: run {
        setUnreadCount(unreadCount + 1, true)
      }
    }

    notification.readAt = null
  }

  private fun archiveNotification(notification: Notification, storePredicate: StorePredicate) {
    if (notification.archivedAt != null) {
      return
    }

    if (notification.seenAt == null) {
      setUnseenCount(unseenCount - 1, true)
    }

    if (notification.readAt == null) {
      setUnreadCount(unreadCount - 1, true)
    }

    if (notification.archivedAt == null) {
      if (!storePredicate.archived) {
        setTotalCount(totalCount - 1, true)
      }
    }

    notification.archivedAt = Date()
  }

  //endregion

  //region Counter methods
  private fun updateCountersWhenDelete(notification: Notification, predicate: StorePredicate) {
    setTotalCount(totalCount - 1, true)

    decreaseUnreadCountIfUnreadPredicate(predicate, notification)
    decreaseUnseenCountIfNotificationWasUnseen(notification)
  }

  private fun decreaseUnreadCountIfUnreadPredicate(predicate: StorePredicate, notification: Notification) {
    if (predicate.read != null) {
      if (predicate.read == false) {
        setUnreadCount(unreadCount - 1, true)
      }
    } else {
      notification.readAt.also {
        setUnreadCount(unreadCount - 1, true)
      }
    }
  }

  private fun decreaseUnseenCountIfNotificationWasUnseen(notification: Notification) {
    if (notification.seenAt == null) {
      setUnseenCount(unseenCount - 1, true)
    }
  }
  //endregion
  //endregion
}
