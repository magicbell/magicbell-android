package com.magicbell.sdk.feature.store

import com.magicbell.sdk.common.query.UserQuery
import com.magicbell.sdk.feature.config.interactor.DeleteConfigInteractor
import com.magicbell.sdk.feature.notification.interactor.ActionNotificationInteractor
import com.magicbell.sdk.feature.notification.interactor.DeleteNotificationInteractor
import com.magicbell.sdk.feature.store.interactor.FetchStorePageInteractor

/**
 * An store director is the class responsible of creating and managing `NotificationStore` objects.
 */
interface StoreDirector {

  /**
   * Returns a notification store for the given predicate.
   *
   * @param predicate Notification store's predicate. Define an scope for the notification store. Read, Seen, Archive, Categories, Topics and inApp.
   */
  fun with(predicate: StorePredicate): NotificationStore

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

internal class PredicateStoreDirector(
  private val userQuery: UserQuery,
  private val fetchStorePageInteractor: FetchStorePageInteractor,
  private val actionNotificationInteractor: ActionNotificationInteractor,
  private val deleteNotificationInteractor: DeleteNotificationInteractor,
  private val deleteConfigInteractor: DeleteConfigInteractor,
) : InternalStoreDirector {

  private val stores: MutableList<NotificationStore> = mutableListOf()

  override fun with(predicate: StorePredicate): NotificationStore {
    return stores.firstOrNull { it.predicate.hashCode() == predicate.hashCode() }?.also { store ->
      return store
    } ?: run {
      val store = NotificationStore(
        predicate,
        userQuery,
        fetchStorePageInteractor,
        actionNotificationInteractor,
        deleteNotificationInteractor
      )

      stores.add(store)
      return store
    }
  }

  // TODO: 16/1/22 create extensions for default store predicates

  override fun disposeWith(predicate: StorePredicate) {
    val notificationIndex = stores.indexOfFirst { it.predicate.hashCode() == predicate.hashCode() }
    if (notificationIndex != -1) {
      val store = stores[notificationIndex]
      //TODO: Remove observer
      stores.removeAt(notificationIndex)
    }
  }

  override suspend fun logout() {
//TODO: Remove observers
    deleteConfigInteractor(userQuery)
  }
}

/**
 * Returns the store for all notifications
 */
fun StoreDirector.forAll(): NotificationStore {
  return with(StorePredicate())
}

/**
 * Returns the store for unread notifications
 */
fun StoreDirector.forUnread(): NotificationStore {
  return with(StorePredicate(read = false))
}

/**
 * Returns the store for read notifications
 */
fun StoreDirector.forRead(): NotificationStore {
  return with(StorePredicate(read = true))
}

/**
 * Return the store for notifications with the given categories
 *
 * @param categories The list of categories
 */
fun StoreDirector.forCategories(categories: List<String>): NotificationStore {
  return with(StorePredicate(categories = categories))
}

/**
 * Return the store for notifications with the given topics
 *
 * @param topics The list of topics
 */
fun StoreDirector.forTopics(topics: List<String>): NotificationStore {
  return with(StorePredicate(topics = topics))
}