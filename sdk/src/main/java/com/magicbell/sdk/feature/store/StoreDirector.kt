package com.magicbell.sdk.feature.store

import com.magicbell.sdk.common.query.UserQuery
import com.magicbell.sdk.common.threading.MainThread
import com.magicbell.sdk.feature.config.interactor.DeleteConfigInteractor
import com.magicbell.sdk.feature.config.interactor.GetConfigInteractor
import com.magicbell.sdk.feature.notification.interactor.ActionNotificationInteractor
import com.magicbell.sdk.feature.notification.interactor.DeleteNotificationInteractor
import com.magicbell.sdk.feature.realtime.StoreRealTime
import com.magicbell.sdk.feature.store.interactor.FetchStorePageInteractor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

/**
 * An store director is the class responsible of creating and managing `NotificationStore` objects.
 */
interface StoreDirector {

  /**
   * Returns a notification store for the given predicate.
   *
   * @param predicate Notification store's predicate. Define an scope for the notification store. Read, Seen, Archive, Categories, Topics and inApp.
   */
  fun build(predicate: StorePredicate): NotificationStore

  /**
   * Disposes a notification store for the given predicate if exists. To be called when a notification store is no longer needed.
   *
   * @param predicate Notification store's predicate.
   */
  fun disposeWith(predicate: StorePredicate)
}

internal interface InternalStoreDirector : StoreDirector {
  suspend fun logout()
}

internal class RealTimeByPredicateStoreDirector(
  private val userQuery: UserQuery,
  private val coroutineContext: CoroutineContext,
  private val coroutineScope: CoroutineScope,
  private val mainThread: MainThread,
  private val fetchStorePageInteractor: FetchStorePageInteractor,
  private val actionNotificationInteractor: ActionNotificationInteractor,
  private val deleteNotificationInteractor: DeleteNotificationInteractor,
  private val getConfigInteractor: GetConfigInteractor,
  private val deleteConfigInteractor: DeleteConfigInteractor,
  private val storeRealTime: StoreRealTime,
) : InternalStoreDirector {

  init {
    startRealTimeConnection()
  }

  private val stores: MutableList<NotificationStore> = mutableListOf()

  private fun startRealTimeConnection() {
    CoroutineScope(coroutineContext).launch {
      runCatching {
        val config = getConfigInteractor(false, userQuery)
        storeRealTime.startListening(config)
      }.onFailure {
        delay(30000)
        startRealTimeConnection()
      }
    }
  }

  override fun build(predicate: StorePredicate): NotificationStore {
    return stores.firstOrNull { it.predicate.hashCode() == predicate.hashCode() }?.also { store ->
      return store
    } ?: run {
      val store = NotificationStore(
        predicate,
        coroutineContext,
        coroutineScope,
        mainThread,
        userQuery,
        fetchStorePageInteractor,
        actionNotificationInteractor,
        deleteNotificationInteractor
      )

      storeRealTime.addObserver(store.realTimeObserver)
      stores.add(store)

      return store
    }
  }

  override fun disposeWith(predicate: StorePredicate) {
    val notificationIndex = stores.indexOfFirst { it.predicate.hashCode() == predicate.hashCode() }
    if (notificationIndex != -1) {
      val store = stores[notificationIndex]
      storeRealTime.removeObserver(store.realTimeObserver)
      stores.removeAt(notificationIndex)
    }
  }

  override suspend fun logout() {
    stores.forEach { storeRealTime.removeObserver(it.realTimeObserver) }
    stores.clear()
    deleteConfigInteractor(userQuery)
    storeRealTime.stopListening()
  }
}

/**
 * Returns the store for all notifications
 */
fun StoreDirector.forAll(): NotificationStore {
  return build(StorePredicate())
}

/**
 * Returns the store for unread notifications
 */
fun StoreDirector.forUnread(): NotificationStore {
  return build(StorePredicate(read = false))
}

/**
 * Returns the store for read notifications
 */
fun StoreDirector.forRead(): NotificationStore {
  return build(StorePredicate(read = true))
}

/**
 * Return the store for notifications with the given category
 *
 * @param category The category
 */
fun StoreDirector.forCategory(category: String): NotificationStore {
  return build(StorePredicate(category = category))
}

/**
 * Return the store for notifications with the given topic
 *
 * @param topic The topic
 */
fun StoreDirector.forTopics(topic: String): NotificationStore {
  return build(StorePredicate(topic = topic))
}