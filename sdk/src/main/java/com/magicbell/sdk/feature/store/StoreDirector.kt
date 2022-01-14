package com.magicbell.sdk.feature.store

import com.magicbell.sdk.common.query.UserQuery
import com.magicbell.sdk.feature.config.interactor.DeleteConfigInteractor
import com.magicbell.sdk.feature.notification.interactor.ActionNotificationInteractor
import com.magicbell.sdk.feature.notification.interactor.DeleteNotificationInteractor
import com.magicbell.sdk.feature.store.interactor.FetchStorePageInteractor

interface StoreDirector {

  fun with(storePredicate: StorePredicate): NotificationStore

  fun disposeWith(storePredicate: StorePredicate)
}

internal interface InternalStoreDirector : StoreDirector {
  suspend fun logout()
}

internal class PredicateStoreDirector(
  private val userQuery: UserQuery,
  private val fetchStorePageInteractor: FetchStorePageInteractor,
  private val actionNotificationInteractor: ActionNotificationInteractor,
  private val deleteNotificationInteractor: DeleteNotificationInteractor,
  private val deleteConfigInteractor: DeleteConfigInteractor
) : InternalStoreDirector {

  private val stores: MutableList<NotificationStore> = mutableListOf()

  override fun with(storePredicate: StorePredicate): NotificationStore {
    return stores.firstOrNull { it.predicate.hashCode() == storePredicate.hashCode() }?.also { store ->
      return store
    } ?: run {
      val store = NotificationStore(
        storePredicate,
        userQuery,
        fetchStorePageInteractor,
        actionNotificationInteractor,
        deleteNotificationInteractor
      )

      stores.add(store)
      return store
    }
  }

  override fun disposeWith(storePredicate: StorePredicate) {
    val notificationIndex = stores.indexOfFirst { it.predicate.hashCode() == storePredicate.hashCode() }
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