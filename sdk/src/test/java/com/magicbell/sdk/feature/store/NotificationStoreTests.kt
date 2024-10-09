package com.magicbell.sdk.feature.store

import com.magicbell.sdk.common.error.MagicBellError
import com.magicbell.sdk.common.network.AnyCursor
import com.magicbell.sdk.common.network.PageInfoMotherObject
import com.magicbell.sdk.common.network.anyNotificationEdgeArray
import com.magicbell.sdk.common.query.UserQuery
import com.magicbell.sdk.common.threading.MainThread
import com.magicbell.sdk.feature.notification.data.NotificationActionQuery.Action.ARCHIVE
import com.magicbell.sdk.feature.notification.data.NotificationActionQuery.Action.MARK_ALL_AS_READ
import com.magicbell.sdk.feature.notification.data.NotificationActionQuery.Action.MARK_ALL_AS_SEEN
import com.magicbell.sdk.feature.notification.data.NotificationActionQuery.Action.MARK_AS_READ
import com.magicbell.sdk.feature.notification.data.NotificationActionQuery.Action.MARK_AS_UNREAD
import com.magicbell.sdk.feature.notification.data.NotificationActionQuery.Action.UNARCHIVE
import com.magicbell.sdk.feature.notification.interactor.ActionNotificationInteractor
import com.magicbell.sdk.feature.notification.interactor.DeleteNotificationInteractor
import com.magicbell.sdk.feature.notification.motherobject.ForceProperty
import com.magicbell.sdk.feature.store.interactor.FetchStorePageInteractor
import com.magicbell.sdk.feature.store.motherobject.StoreMotherObject
import com.magicbell.sdk.feature.store.motherobject.givenPageStore
import com.magicbell.sdk.feature.store.utils.ContentObserverMock
import com.magicbell.sdk.feature.store.utils.InitialNotificationStoreCounts
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.concurrent.Executors
import kotlin.random.Random

internal class NotificationStoreTests {
  private val defaultEdgeArraySize = 20
  private val anyIndexForDefaultEdgeArraySize by lazy { Random.nextInt(0, defaultEdgeArraySize) }

  private val userQuery = UserQuery.createEmail("javier@mobilejazz.com")

  private val mainThread = object : MainThread {
    override fun post(run: () -> Unit) {
      run()
    }
  }
  private val coroutineContext = Executors.newFixedThreadPool(1).asCoroutineDispatcher()

  var fetchStorePageInteractor: FetchStorePageInteractor = mockk()
  var actionNotificationInteractor: ActionNotificationInteractor = mockk()
  var deleteNotificationInteractor: DeleteNotificationInteractor = mockk()

  lateinit var notificationStore: NotificationStore

  private val mainThreadSurrogate = newSingleThreadContext("UI thread")

  @BeforeEach
  fun setUp() {
    Dispatchers.setMain(mainThreadSurrogate)
  }

  @AfterEach
  fun tearDown() {
    Dispatchers.resetMain() // reset the main dispatcher to the original Main dispatcher
    mainThreadSurrogate.close()
  }

  private fun createNotificationStore(
    predicate: StorePredicate,
    fetchStoreExpectedResult: Result<StorePage>
  ): NotificationStore {
    fetchStoreExpectedResult.fold(onSuccess = {
      coEvery { fetchStorePageInteractor.invoke(any(), any(), any()) } returns fetchStoreExpectedResult.getOrThrow()
    }, onFailure = {
      coEvery { fetchStorePageInteractor.invoke(any(), any(), any()) } throws it
    })

    notificationStore = NotificationStore(
      predicate,
      coroutineContext,
      CoroutineScope(mainThreadSurrogate),
      mainThread,
      userQuery,
      fetchStorePageInteractor,
      actionNotificationInteractor,
      deleteNotificationInteractor
    )

    return notificationStore
  }

  @Test
  fun test_init_shouldReturnEmptyNotifications() = runBlocking {
    val predicate = StorePredicate()
    val store = createNotificationStore(
      StorePredicate(),
      Result.success(givenPageStore(predicate, defaultEdgeArraySize))
    )
    store.count.shouldBeExactly(0)
    Unit
  }

  @Test
  fun test_fetch_withDefaultStorePredicate_shouldReturnNotification() = runBlocking {
    // GIVEN
    val predicate = StorePredicate()
    val storePage = givenPageStore(predicate, defaultEdgeArraySize)
    val store = createNotificationStore(
      predicate,
      Result.success(storePage)
    )

    // WHEN
    val notifications = store.fetch().getOrThrow()

    // THEN
    coVerify(exactly = 1, timeout = 1000) { fetchStorePageInteractor.invoke(any(), any(), any()) }
    store.size.shouldBeExactly(defaultEdgeArraySize)
    notifications.size.shouldBeExactly(defaultEdgeArraySize)
    notifications.forEachIndexed { index, notification ->
      store[index].id.shouldBe(notification.id)
    }
  }

  @Test
  fun test_store_allNotifications_shouldReturnAllNotifications() = runBlocking {
    // GIVEN
    val predicate = StorePredicate()
    val storePage = givenPageStore(predicate, defaultEdgeArraySize)
    val store = createNotificationStore(
      predicate,
      Result.success(storePage)
    )

    // WHEN
    store.fetch()

    // THEN
    coVerify(exactly = 1, timeout = 1000) { fetchStorePageInteractor.invoke(any(), any(), any()) }
    store.size.shouldBeExactly(defaultEdgeArraySize)
    storePage.edges.map { it.node.id }.shouldBe(store.notifications.map { it.id })
  }

  @Test
  fun test_fetch_withDefaultStorePredicateAndError_shouldReturnError() = runBlocking {
    // GIVEN
    val predicate = StorePredicate()
    val store = createNotificationStore(
      predicate,
      Result.failure(MagicBellError("Error"))
    )

    // WHEN
    var errorExpected: Exception? = null
    try {
      store.fetch().getOrThrow()
    } catch (e: Exception) {
      errorExpected = e
    }

    // THEN
    coVerify(exactly = 1, timeout = 1000) { fetchStorePageInteractor.invoke(any(), any(), any()) }
    store.size.shouldBeExactly(0)
    errorExpected.shouldNotBeNull()
    Unit
  }

  @Test
  fun test_refresh_withDefaultStorePredicate_shouldRefreshContent() = runBlocking {
    // GIVEN
    val predicate = StorePredicate()
    val storePage = givenPageStore(predicate, defaultEdgeArraySize)
    val store = createNotificationStore(
      predicate,
      Result.success(storePage)
    )

    // WHEN
    val notifications = store.refresh().getOrThrow()

    // THEN
    coVerify(exactly = 1, timeout = 1000) { fetchStorePageInteractor.invoke(any(), any(), any()) }
    store.size.shouldBeExactly(defaultEdgeArraySize)
    notifications.size.shouldBeExactly(defaultEdgeArraySize)
    notifications.forEachIndexed { index, notification ->
      store[index].id.shouldBe(notification.id)
    }
  }

  @Test
  fun test_refresh_withDefaultStorePredicateAndError_shouldReturnError() = runBlocking {
    // GIVEN
    val predicate = StorePredicate()
    val store = createNotificationStore(
      predicate,
      Result.failure(MagicBellError("Error"))
    )

    // WHEN
    var errorExpected: Exception? = null
    try {
      store.refresh().getOrThrow()
    } catch (e: Exception) {
      errorExpected = e
    }

    // THEN
    coVerify(exactly = 1, timeout = 1000) { fetchStorePageInteractor.invoke(any(), any(), any()) }
    store.size.shouldBeExactly(0)
    errorExpected.shouldNotBeNull()
    Unit
  }

  @Test
  fun test_fetch_withPagination_shouldReturnTwoNotificationPages() = runBlocking {
    // GIVEN
    val predicate = StorePredicate()
    val storePage = StoreMotherObject.createStorePage(
      anyNotificationEdgeArray(predicate, defaultEdgeArraySize, ForceProperty.None),
      PageInfoMotherObject.createPageInfo(AnyCursor.ANY.value, hasNextPage = true)
    )
    val store = createNotificationStore(predicate, Result.success(storePage))

    // WHEN
    store.fetch().getOrThrow()

    // THEN
    coVerify(exactly = 1, timeout = 1000) { fetchStorePageInteractor.invoke(any(), any(), any()) }
    store.size.shouldBeExactly(defaultEdgeArraySize)

    // WHEN
    val notifications = store.fetch().getOrThrow()

    // THEN
    coVerify(exactly = 2, timeout = 1000) { fetchStorePageInteractor.invoke(any(), any(), any()) }
    store.size.shouldBeExactly(defaultEdgeArraySize * 2)
    notifications.size.shouldBeExactly(defaultEdgeArraySize)
    store.mapIndexed { index, notification ->
      notifications[index].id.shouldBe(notification.id)
    }
  }

  @Test
  fun test_fetch_withoutPagination_shouldReturnEmptyArray() = runBlocking {
    // GIVEN
    val predicate = StorePredicate()
    val storePage = StoreMotherObject.createStorePage(
      anyNotificationEdgeArray(predicate, defaultEdgeArraySize, ForceProperty.None),
      PageInfoMotherObject.createPageInfo(AnyCursor.ANY.value, hasNextPage = false)
    )
    val store = createNotificationStore(predicate, Result.success(storePage))

    // WHEN
    store.fetch().getOrThrow()

    // THEN
    coVerify(exactly = 1, timeout = 1000) { fetchStorePageInteractor.invoke(any(), any(), any()) }
    store.size.shouldBeExactly(defaultEdgeArraySize)

    // WHEN
    val notifications = store.fetch().getOrThrow()

    // THEN
    coVerify(exactly = 1, timeout = 1000) { fetchStorePageInteractor.invoke(any(), any(), any()) }
    store.size.shouldBeExactly(defaultEdgeArraySize)
    notifications.size.shouldBeExactly(0)
    Unit
  }

  @Test
  fun test_refresh_twoTimes_shouldReturnSamePage() = runBlocking {
    // GIVEN
    val predicate = StorePredicate()
    val storePage = StoreMotherObject.createStorePage(
      anyNotificationEdgeArray(predicate, defaultEdgeArraySize, ForceProperty.None),
      PageInfoMotherObject.createPageInfo(AnyCursor.ANY.value, hasNextPage = true)
    )
    val store = createNotificationStore(predicate, Result.success(storePage))

    // WHEN
    store.refresh().getOrThrow()

    // THEN
    coVerify(exactly = 1, timeout = 1000) { fetchStorePageInteractor.invoke(any(), any(), any()) }
    store.size.shouldBeExactly(defaultEdgeArraySize)
    storePage.edges.mapIndexed { index, edge ->
      store[index].id.shouldBe(edge.node.id)
    }

    // WHEN
    store.refresh().getOrThrow()

    // THEN
    coVerify(exactly = 2, timeout = 1000) { fetchStorePageInteractor.invoke(any(), any(), any()) }
    store.size.shouldBeExactly(defaultEdgeArraySize)
    storePage.edges.mapIndexed { index, edge ->
      store[index].id.shouldBe(edge.node.id)
    }
    Unit
  }

  @Test
  fun test_fetch_withPageInfoHasNextPageTrue_shouldConfigurePaginationTrue() = runBlocking {
    // GIVEN
    val predicate = StorePredicate()
    val storePage = StoreMotherObject.createStorePage(
      anyNotificationEdgeArray(predicate, defaultEdgeArraySize, ForceProperty.None),
      PageInfoMotherObject.createPageInfo(AnyCursor.ANY.value, hasNextPage = true)
    )
    val store = createNotificationStore(predicate, Result.success(storePage))
    store.hasNextPage.shouldBeTrue()

    // WHEN
    store.fetch().getOrThrow()

    // THEN
    store.hasNextPage.shouldBeTrue()
  }

  @Test
  fun test_fetch_withPageInfoHasNextPageFalse_shouldConfigurePaginationFalse() = runBlocking {
    // GIVEN
    val predicate = StorePredicate()
    val storePage = StoreMotherObject.createStorePage(
      anyNotificationEdgeArray(predicate, defaultEdgeArraySize, ForceProperty.None),
      PageInfoMotherObject.createPageInfo(AnyCursor.ANY.value, hasNextPage = false)
    )
    val store = createNotificationStore(predicate, Result.success(storePage))
    store.hasNextPage.shouldBeTrue()

    // WHEN
    store.fetch().getOrThrow()


    // THEN
    store.hasNextPage.shouldBeFalse()
  }

  @Test
  fun test_refresh_withPageInfoHasNextPageFalse_shouldConfigurePaginationFalse() = runBlocking {
    // GIVEN
    val predicate = StorePredicate()
    val storePage = StoreMotherObject.createStorePage(
      anyNotificationEdgeArray(predicate, defaultEdgeArraySize, ForceProperty.None),
      PageInfoMotherObject.createPageInfo(AnyCursor.ANY.value, hasNextPage = false)
    )
    val store = createNotificationStore(predicate, Result.success(storePage))
    store.hasNextPage.shouldBeTrue()

    // WHEN
    store.refresh().getOrThrow()

    // THEN
    store.hasNextPage.shouldBeFalse()
  }

  @Test
  fun test_deleteNotification_withDefaultStorePredicate_shouldCallActionNotificationInteractor() = runBlocking {
    // GIVEN
    val predicate = StorePredicate()
    val storePage = StoreMotherObject.createStorePage(
      anyNotificationEdgeArray(predicate, defaultEdgeArraySize, ForceProperty.Read),
      PageInfoMotherObject.createPageInfo(AnyCursor.ANY.value, hasNextPage = false)
    )
    val store = createNotificationStore(predicate, Result.success(storePage))
    // WHEN
    store.fetch().getOrThrow()
    val removeIndex = anyIndexForDefaultEdgeArraySize
    val removedNotification = store[removeIndex]
    store.delete(store[removeIndex])

    // THEN
    coVerify(exactly = 1, timeout = 1000) { fetchStorePageInteractor.invoke(any(), any(), any()) }
    coVerify(exactly = 1, timeout = 1000) { deleteNotificationInteractor.invoke(removedNotification.id, userQuery) }
  }

  @Test
  fun test_deleteNotification_withError_shouldReturnError() = runBlocking {
    // GIVEN
    val predicate = StorePredicate()
    val storePage = StoreMotherObject.createStorePage(
      anyNotificationEdgeArray(predicate, defaultEdgeArraySize, ForceProperty.Read),
      PageInfoMotherObject.createPageInfo(AnyCursor.ANY.value, hasNextPage = false)
    )
    val store = createNotificationStore(predicate, Result.success(storePage))


    // WHEN
    store.fetch().getOrThrow()
    val removeIndex = anyIndexForDefaultEdgeArraySize
    val removedNotification = store[removeIndex]

    coEvery { deleteNotificationInteractor.invoke(removedNotification.id, userQuery) } throws MagicBellError("Error")

    var errorExpected: Exception? = null
    try {
      store.delete(store[removeIndex]).getOrThrow()
    } catch (e: Exception) {
      errorExpected = e
    }

    // THEN
    coVerify(exactly = 1, timeout = 1000) { fetchStorePageInteractor.invoke(any(), any(), any()) }
    coVerify(exactly = 1, timeout = 1000) { deleteNotificationInteractor.invoke(removedNotification.id, userQuery) }
    errorExpected.shouldNotBeNull()
    Unit
  }

  @Test
  fun test_deleteNotification_withDefaultStorePredicateAndReadNotification_shouldRemoveNotificationAndSameUnreadCount() = runBlocking {
    // GIVEN
    val predicate = StorePredicate()
    val storePage = StoreMotherObject.createStorePage(
      anyNotificationEdgeArray(predicate, defaultEdgeArraySize, ForceProperty.Read),
      PageInfoMotherObject.createPageInfo(AnyCursor.ANY.value, hasNextPage = false)
    )
    val store = createNotificationStore(predicate, Result.success(storePage))

    // WHEN
    store.fetch().getOrThrow()
    val initialCounts = InitialNotificationStoreCounts(store)
    val removeIndex = anyIndexForDefaultEdgeArraySize
    val removedNotification = store[removeIndex]

    coEvery { deleteNotificationInteractor.invoke(removedNotification.id, userQuery) } returns Unit

    store.delete(store[removeIndex]).getOrThrow()

    // THEN
    store.totalCount.shouldBeExactly(initialCounts.totalCount - 1)
    store.unreadCount.shouldBeExactly(initialCounts.unreadCount - 1)
    if (store.size > removeIndex) {
      store[removeIndex].id.shouldNotBe(removedNotification.id)
    }
  }

  @Test
  fun test_deleteNotification_withDefaultStorePredicateAndUnreadNotification_shouldRemoveNotificationAndDifferentUnreadCount() = runBlocking {
    // GIVEN
    val predicate = StorePredicate()
    val storePage = StoreMotherObject.createStorePage(
      anyNotificationEdgeArray(predicate, defaultEdgeArraySize, ForceProperty.Unread),
      PageInfoMotherObject.createPageInfo(AnyCursor.ANY.value, hasNextPage = false)
    )
    val store = createNotificationStore(predicate, Result.success(storePage))

    // WHEN
    store.fetch().getOrThrow()
    val initialCounts = InitialNotificationStoreCounts(store)
    val removeIndex = anyIndexForDefaultEdgeArraySize
    val removedNotification = store[removeIndex]

    coEvery { deleteNotificationInteractor.invoke(removedNotification.id, userQuery) } returns Unit
    store.delete(store[removeIndex]).getOrThrow()

    // THEN
    store.totalCount.shouldBeExactly(initialCounts.totalCount - 1)
    store.unreadCount.shouldBeExactly(initialCounts.unreadCount - 1)
    if (store.size > removeIndex) {
      store[removeIndex].id.shouldNotBe(removedNotification.id)
    }
  }

  @Test
  fun test_deleteNotification_withDefaultStorePredicateAndSeenNotification_shouldRemoveNotificationAndSameUnseenCount() = runBlocking {
    // GIVEN
    val predicate = StorePredicate()
    val storePage = StoreMotherObject.createStorePage(
      anyNotificationEdgeArray(predicate, defaultEdgeArraySize, ForceProperty.Seen),
      PageInfoMotherObject.createPageInfo(AnyCursor.ANY.value, hasNextPage = false)
    )
    val store = createNotificationStore(predicate, Result.success(storePage))

    // WHEN
    store.fetch().getOrThrow()
    val initialCounts = InitialNotificationStoreCounts(store)
    val removeIndex = anyIndexForDefaultEdgeArraySize
    val removedNotification = store[removeIndex]

    coEvery { deleteNotificationInteractor.invoke(removedNotification.id, userQuery) } returns Unit
    store.delete(store[removeIndex]).getOrThrow()

    // THEN
    store.totalCount.shouldBeExactly(initialCounts.totalCount - 1)
    store.unseenCount.shouldBeExactly(initialCounts.unseenCount)
    if (store.size > removeIndex) {
      store[removeIndex].id.shouldNotBe(removedNotification.id)
    }
  }

  @Test
  fun test_deleteNotification_withDefaultStorePredicateAndUnseenNotification_shouldRemoveNotificationAndDifferentUnseenCount() = runBlocking {
    // GIVEN
    val predicate = StorePredicate()
    val storePage = StoreMotherObject.createStorePage(
      anyNotificationEdgeArray(predicate, defaultEdgeArraySize, ForceProperty.Unseen),
      PageInfoMotherObject.createPageInfo(AnyCursor.ANY.value, hasNextPage = false)
    )
    val store = createNotificationStore(predicate, Result.success(storePage))

    // WHEN
    store.fetch().getOrThrow()
    val initialCounts = InitialNotificationStoreCounts(store)
    val removeIndex = anyIndexForDefaultEdgeArraySize
    val removedNotification = store[removeIndex]

    coEvery { deleteNotificationInteractor.invoke(removedNotification.id, userQuery) } returns Unit
    store.delete(store[removeIndex]).getOrThrow()

    // THEN
    store.totalCount.shouldBeExactly(initialCounts.totalCount - 1)
    store.unseenCount.shouldBeExactly(initialCounts.unseenCount - 1)
    if (store.size > removeIndex) {
      store[removeIndex].id.shouldNotBe(removedNotification.id)
    }
  }

  @Test
  fun test_deleteNotification_withReadStorePredicate_shouldRemoveNotification() = runBlocking {
    // GIVEN
    val predicate = StorePredicate(read = true)
    val storePage = StoreMotherObject.createStorePage(
      anyNotificationEdgeArray(predicate, defaultEdgeArraySize, ForceProperty.None),
      PageInfoMotherObject.createPageInfo(AnyCursor.ANY.value, hasNextPage = false)
    )
    val store = createNotificationStore(predicate, Result.success(storePage))

    // WHEN
    store.fetch().getOrThrow()
    val initialCounts = InitialNotificationStoreCounts(store)
    val removeIndex = anyIndexForDefaultEdgeArraySize
    coEvery { deleteNotificationInteractor.invoke(any(), any()) } returns Unit
    store.delete(store[removeIndex]).getOrThrow()

    // THEN
    store.totalCount.shouldBeExactly(initialCounts.totalCount - 1)
    store.unreadCount.shouldBeExactly(0)
    Unit
  }

  @Test
  fun test_deleteNotification_withUnreadStorePredicate_shouldRemoveNotification() = runBlocking {
    // GIVEN
    val predicate = StorePredicate(read = false)
    val storePage = StoreMotherObject.createStorePage(
      anyNotificationEdgeArray(predicate, defaultEdgeArraySize, ForceProperty.None),
      PageInfoMotherObject.createPageInfo(AnyCursor.ANY.value, hasNextPage = false)
    )
    val store = createNotificationStore(predicate, Result.success(storePage))

    // WHEN
    store.fetch().getOrThrow()
    store.size.shouldBeExactly(defaultEdgeArraySize)
    val initialCounts = InitialNotificationStoreCounts(store)
    val removeIndex = anyIndexForDefaultEdgeArraySize
    coEvery { deleteNotificationInteractor.invoke(any(), any()) } returns Unit
    store.delete(store[removeIndex]).getOrThrow()


    // THEN
    store.totalCount.shouldBeExactly(initialCounts.totalCount - 1)
    store.unreadCount.shouldBeExactly(initialCounts.unreadCount - 1)
    Unit
  }

  @Test
  fun test_deleteNotification_withUnreadStorePredicateWithUnseenNotifications_shouldRemoveNotificationAndUpdateUnseeCount() = runBlocking {
    // GIVEN
    val predicate = StorePredicate(read = false)
    val storePage = StoreMotherObject.createStorePage(
      anyNotificationEdgeArray(predicate, defaultEdgeArraySize, ForceProperty.Unseen),
      PageInfoMotherObject.createPageInfo(AnyCursor.ANY.value, hasNextPage = false)
    )
    val store = createNotificationStore(predicate, Result.success(storePage))

    // WHEN
    store.fetch().getOrThrow()
    store.size.shouldBeExactly(defaultEdgeArraySize)
    val initialCounts = InitialNotificationStoreCounts(store)
    val removeIndex = anyIndexForDefaultEdgeArraySize
    coEvery { deleteNotificationInteractor.invoke(any(), any()) } returns Unit
    store.delete(store[removeIndex]).getOrThrow()

    // THEN
    store.totalCount.shouldBeExactly(initialCounts.totalCount - 1)
    store.unreadCount.shouldBeExactly(initialCounts.unreadCount - 1)
    store.unseenCount.shouldBeExactly(initialCounts.unseenCount - 1)
    Unit
  }

  @Test
  fun test_deleteNotification_withUnreadStorePredicateWithSeenNotifications_shouldRemoveNotificationAndSameUnseenCount() = runBlocking {
    // GIVEN
    val predicate = StorePredicate(read = false)
    val storePage = StoreMotherObject.createStorePage(
      anyNotificationEdgeArray(predicate, defaultEdgeArraySize, ForceProperty.Seen),
      PageInfoMotherObject.createPageInfo(AnyCursor.ANY.value, hasNextPage = false)
    )
    val store = createNotificationStore(predicate, Result.success(storePage))

    // WHEN
    store.fetch().getOrThrow()
    val initialCounts = InitialNotificationStoreCounts(store)
    val removeIndex = anyIndexForDefaultEdgeArraySize
    coEvery { deleteNotificationInteractor.invoke(any(), any()) } returns Unit
    store.delete(store[removeIndex]).getOrThrow()

    // THEN
    store.totalCount.shouldBeExactly(initialCounts.totalCount - 1)
    store.unreadCount.shouldBeExactly(initialCounts.unreadCount - 1)
    store.unseenCount.shouldBeExactly(initialCounts.unseenCount)
    Unit
  }

  @Test
  fun test_markNotificationAsRead_withDefaultStorePredicate_shouldCallActioNotificationInteractor() = runBlocking {
    // GIVEN
    val predicate = StorePredicate()
    val storePage = givenPageStore(predicate, defaultEdgeArraySize)
    val store = createNotificationStore(
      predicate, Result.success(storePage)
    )

    // WHEN
    store.fetch().getOrThrow()
    val chosenIndex = anyIndexForDefaultEdgeArraySize
    val markReadNotification = store[chosenIndex]
    coEvery { actionNotificationInteractor.invoke(MARK_AS_READ, markReadNotification.id, userQuery) } returns Unit
    store.markAsRead(store[chosenIndex]).getOrThrow()

    // THEN
    coVerify(exactly = 1, timeout = 1000) { fetchStorePageInteractor.invoke(any(), any(), any()) }
    coVerify(exactly = 1, timeout = 1000) { actionNotificationInteractor.invoke(MARK_AS_READ, markReadNotification.id, userQuery) }
  }

  @Test
  fun test_markNotificationAsRead_withError_shouldReturnError() = runBlocking {
    // GIVEN
    val predicate = StorePredicate()
    val storePage = givenPageStore(predicate, defaultEdgeArraySize)
    val store = createNotificationStore(
      predicate, Result.success(storePage)
    )

    // WHEN
    store.fetch().getOrThrow()

    val chosenIndex = anyIndexForDefaultEdgeArraySize
    val markReadNotification = store[chosenIndex]

    var errorExpected: Exception? = null
    try {
      store.markAsRead(store[chosenIndex]).getOrThrow()
    } catch (e: Exception) {
      errorExpected = e
    }

    // THEN
    coVerify(exactly = 1, timeout = 1000) { fetchStorePageInteractor.invoke(any(), any(), any()) }
    coVerify(exactly = 1, timeout = 1000) { actionNotificationInteractor.invoke(MARK_AS_READ, markReadNotification.id, userQuery) }
    errorExpected.shouldNotBeNull()
    Unit
  }

  @Test
  fun test_markNotificationAsRead_withDefaultStorePredicate_shouldMarkAsReadNotification() = runBlocking {
    // GIVEN
    val predicate = StorePredicate()
    val storePage = givenPageStore(predicate, defaultEdgeArraySize)
    val store = createNotificationStore(
      predicate, Result.success(storePage)
    )

    // WHEN
    store.fetch().getOrThrow()
    val initialCounts = InitialNotificationStoreCounts(store)
    val chosenIndex = anyIndexForDefaultEdgeArraySize
    val markReadNotification = store[chosenIndex]
    coEvery { actionNotificationInteractor.invoke(MARK_AS_READ, markReadNotification.id, userQuery) } returns Unit
    store.markAsRead(store[chosenIndex]).getOrThrow()

    // THEN
    store[chosenIndex].readAt.shouldNotBeNull()
    store.totalCount.shouldBeExactly(initialCounts.totalCount)
  }

  @Test
  fun test_markNotificationAsRead_withDefaultStorePredicateAndUnreadNotification_shouldMarkAsReadNotificationAndUpdateUnreadCounter() = runBlocking {
    // GIVEN
    val predicate = StorePredicate()
    val storePage = givenPageStore(predicate, defaultEdgeArraySize, forceProperty = ForceProperty.Unread)
    val store = createNotificationStore(
      predicate, Result.success(storePage)
    )

    // WHEN
    store.fetch().getOrThrow()
    val initialCounts = InitialNotificationStoreCounts(store)
    val chosenIndex = anyIndexForDefaultEdgeArraySize
    val markReadNotification = store[chosenIndex]
    coEvery { actionNotificationInteractor.invoke(MARK_AS_READ, markReadNotification.id, userQuery) } returns Unit
    store.markAsRead(store[chosenIndex]).getOrThrow()

    // THEN
    store[chosenIndex].readAt.shouldNotBeNull()
    store.totalCount.shouldBeExactly(initialCounts.totalCount)
    store.unreadCount.shouldBeExactly(initialCounts.unreadCount - 1)
    Unit
  }

  @Test
  fun test_markNotificationAsRead_withDefaultStorePredicateAndReadNotification_shouldMarkAsReadNotificationAndSameUnreadCounter() = runBlocking {
    // GIVEN
    val predicate = StorePredicate()
    val storePage = givenPageStore(predicate, defaultEdgeArraySize, forceProperty = ForceProperty.Read)
    val store = createNotificationStore(
      predicate, Result.success(storePage)
    )

    // WHEN
    store.fetch().getOrThrow()
    val initialCounts = InitialNotificationStoreCounts(store)
    val chosenIndex = anyIndexForDefaultEdgeArraySize
    val markReadNotification = store[chosenIndex]
    coEvery { actionNotificationInteractor.invoke(MARK_AS_READ, markReadNotification.id, userQuery) } returns Unit
    store.markAsRead(store[chosenIndex]).getOrThrow()

    // THEN
    store[chosenIndex].readAt.shouldNotBeNull()
    store.totalCount.shouldBeExactly(initialCounts.totalCount)
    store.unreadCount.shouldBeExactly(initialCounts.unreadCount)
    Unit
  }

  @Test
  fun test_markNotificationAsRead_withDefaultStorePredicateAndUnseenNotification_shouldMarkAsReadNotificationAndUpdateUnseenCounter() = runBlocking {
    // GIVEN
    val predicate = StorePredicate()
    val storePage = givenPageStore(predicate, defaultEdgeArraySize, forceProperty = ForceProperty.Unseen)
    val store = createNotificationStore(
      predicate, Result.success(storePage)
    )

    // WHEN
    store.fetch().getOrThrow()
    val initialCounts = InitialNotificationStoreCounts(store)
    val chosenIndex = anyIndexForDefaultEdgeArraySize
    val markReadNotification = store[chosenIndex]
    coEvery { actionNotificationInteractor.invoke(MARK_AS_READ, markReadNotification.id, userQuery) } returns Unit
    store.markAsRead(store[chosenIndex]).getOrThrow()

    // THEN
    store[chosenIndex].readAt.shouldNotBeNull()
    store.totalCount.shouldBeExactly(initialCounts.totalCount)
    store.unseenCount.shouldBeExactly(initialCounts.unseenCount - 1)
    Unit
  }

  @Test
  fun test_markNotificationAsRead_withDefaultStorePredicateAndSeenNotification_shouldMarkAsReadNotificationAndSameUnseenCounter() = runBlocking {
    // GIVEN
    val predicate = StorePredicate()
    val storePage = givenPageStore(predicate, defaultEdgeArraySize, forceProperty = ForceProperty.Seen)
    val store = createNotificationStore(
      predicate, Result.success(storePage)
    )

    // WHEN
    store.fetch().getOrThrow()
    val initialCounts = InitialNotificationStoreCounts(store)
    val chosenIndex = anyIndexForDefaultEdgeArraySize
    val markReadNotification = store[chosenIndex]
    coEvery { actionNotificationInteractor.invoke(MARK_AS_READ, markReadNotification.id, userQuery) } returns Unit
    store.markAsRead(store[chosenIndex]).getOrThrow()

    // THEN
    store[chosenIndex].readAt.shouldNotBeNull()
    store.totalCount.shouldBeExactly(initialCounts.totalCount)
    store.unseenCount.shouldBeExactly(initialCounts.unseenCount)
    Unit
  }

  @Test
  fun test_markNotificationAsRead_withUnreadStorePredicate_shouldMarkAsReadNotification() = runBlocking {
    // GIVEN
    val predicate = StorePredicate(read = false)
    val storePage = givenPageStore(predicate, defaultEdgeArraySize, forceProperty = ForceProperty.Unread)
    val store = createNotificationStore(
      predicate, Result.success(storePage)
    )

    // WHEN
    store.fetch().getOrThrow()
    val initialCounts = InitialNotificationStoreCounts(store)
    val chosenIndex = anyIndexForDefaultEdgeArraySize
    val markReadNotification = store[chosenIndex]
    coEvery { actionNotificationInteractor.invoke(MARK_AS_READ, markReadNotification.id, userQuery) } returns Unit
    store.markAsRead(store[chosenIndex]).getOrThrow()

    // THEN
    store[chosenIndex].readAt.shouldNotBeNull()
    store.totalCount.shouldBeExactly(initialCounts.totalCount - 1)
    store.unreadCount.shouldBeExactly(initialCounts.unreadCount - 1)
    Unit
  }

  @Test
  fun test_markNotificationAsRead_withUnreadStorePredicateAndUnseenNotification_shouldMarkAsReadNotificationAndUpdateUnseenCount() = runBlocking {
    // GIVEN
    val predicate = StorePredicate(read = false)
    val storePage = givenPageStore(predicate, defaultEdgeArraySize, forceProperty = ForceProperty.Unseen)
    val store = createNotificationStore(
      predicate, Result.success(storePage)
    )

    // WHEN
    store.fetch().getOrThrow()
    val initialCounts = InitialNotificationStoreCounts(store)
    val chosenIndex = anyIndexForDefaultEdgeArraySize
    val markReadNotification = store[chosenIndex]
    coEvery { actionNotificationInteractor.invoke(MARK_AS_READ, markReadNotification.id, userQuery) } returns Unit
    store.markAsRead(store[chosenIndex]).getOrThrow()

    // THEN
    store[chosenIndex].readAt.shouldNotBeNull()
    store.totalCount.shouldBeExactly(initialCounts.totalCount - 1)
    store.unseenCount.shouldBeExactly(initialCounts.unseenCount - 1)
    Unit
  }

  @Test
  fun test_markNotificationAsRead_withUnreadStorePredicateAndSeenNotification_shouldMarkAsReadNotificationAndSameUnseenCount() = runBlocking {
    // GIVEN
    val predicate = StorePredicate(read = false)
    val storePage = givenPageStore(predicate, defaultEdgeArraySize, forceProperty = ForceProperty.Seen)
    val store = createNotificationStore(
      predicate, Result.success(storePage)
    )

    // WHEN
    store.fetch().getOrThrow()
    val initialCounts = InitialNotificationStoreCounts(store)
    val chosenIndex = anyIndexForDefaultEdgeArraySize
    val markReadNotification = store[chosenIndex]
    coEvery { actionNotificationInteractor.invoke(MARK_AS_READ, markReadNotification.id, userQuery) } returns Unit
    store.markAsRead(store[chosenIndex]).getOrThrow()

    // THEN
    store[chosenIndex].readAt.shouldNotBeNull()
    store.totalCount.shouldBeExactly(initialCounts.totalCount - 1)
    store.unseenCount.shouldBeExactly(initialCounts.unseenCount)
    Unit
  }

  @Test
  fun test_markNotificationAsUnread_withDefaultStorePredicate_shouldCallActioNotificationInteractor() = runBlocking {
    // GIVEN
    val predicate = StorePredicate()
    val storePage = givenPageStore(predicate, defaultEdgeArraySize)
    val store = createNotificationStore(
      predicate, Result.success(storePage)
    )
    // WHEN
    store.fetch().getOrThrow()
    val initialCounts = InitialNotificationStoreCounts(store)
    val chosenIndex = anyIndexForDefaultEdgeArraySize
    val markReadNotification = store[chosenIndex]
    coEvery { actionNotificationInteractor.invoke(MARK_AS_UNREAD, markReadNotification.id, userQuery) } returns Unit
    store.markAsUnread(store[chosenIndex]).getOrThrow()

    // THEN
    coVerify(exactly = 1, timeout = 1000) { fetchStorePageInteractor.invoke(any(), any(), any()) }
    coVerify { actionNotificationInteractor.invoke(MARK_AS_UNREAD, markReadNotification.id, userQuery) }
  }

  @Test
  fun test_markNotificationAsUnread_withDefaultStorePredicate_shouldMarkAsUnreadNotification() = runBlocking {
    // GIVEN
    val predicate = StorePredicate()
    val storePage = givenPageStore(predicate, defaultEdgeArraySize)
    val store = createNotificationStore(
      predicate, Result.success(storePage)
    )

    // WHEN
    store.fetch().getOrThrow()
    val initialCounts = InitialNotificationStoreCounts(store)
    val chosenIndex = anyIndexForDefaultEdgeArraySize
    val markReadNotification = store[chosenIndex]
    coEvery { actionNotificationInteractor.invoke(MARK_AS_UNREAD, markReadNotification.id, userQuery) } returns Unit
    store.markAsUnread(store[chosenIndex]).getOrThrow()

    // THEN
    store[chosenIndex].readAt.shouldBeNull()
    store.totalCount.shouldBeExactly(initialCounts.totalCount)
    store.unseenCount.shouldBeExactly(initialCounts.unseenCount)
    Unit
  }

  @Test
  fun test_markNotificationAsUnread_withDefaultStorePredicateAndReadNotification_shouldMarkAsUnreadNotificationAndUpdateUnreadCount() = runBlocking {
    // GIVEN
    val predicate = StorePredicate()
    val storePage = givenPageStore(predicate, defaultEdgeArraySize, forceProperty = ForceProperty.Read)
    val store = createNotificationStore(
      predicate, Result.success(storePage)
    )

    // WHEN
    store.fetch().getOrThrow()
    val initialCounts = InitialNotificationStoreCounts(store)
    val chosenIndex = anyIndexForDefaultEdgeArraySize
    val markReadNotification = store[chosenIndex]
    coEvery { actionNotificationInteractor.invoke(MARK_AS_UNREAD, markReadNotification.id, userQuery) } returns Unit
    store.markAsUnread(store[chosenIndex]).getOrThrow()

    // THEN
    store[chosenIndex].readAt.shouldBeNull()
    store.totalCount.shouldBeExactly(initialCounts.totalCount)
    store.unreadCount.shouldBeExactly(initialCounts.unreadCount + 1)
    Unit
  }

  @Test
  fun test_markNotificationAsUnread_withReadStorePredicate_shouldRemoveNotification() = runBlocking {
    // GIVEN
    val predicate = StorePredicate(read = true)
    val storePage = givenPageStore(predicate, defaultEdgeArraySize, forceProperty = ForceProperty.Read)
    val store = createNotificationStore(
      predicate, Result.success(storePage)
    )

    // WHEN
    store.fetch().getOrThrow()
    val initialCounts = InitialNotificationStoreCounts(store)
    val chosenIndex = anyIndexForDefaultEdgeArraySize
    val markReadNotification = store[chosenIndex]
    coEvery { actionNotificationInteractor.invoke(MARK_AS_UNREAD, markReadNotification.id, userQuery) } returns Unit
    store.markAsUnread(store[chosenIndex]).getOrThrow()

    // THEN
    store[chosenIndex].readAt.shouldBeNull()
    store.totalCount.shouldBeExactly(initialCounts.totalCount - 1)
    store.unreadCount.shouldBeExactly(0)
    store.unseenCount.shouldBeExactly(initialCounts.unseenCount)
    Unit
  }

  @Test
  fun test_markNotificationAsUnread_withUnreadStorePredicate_shouldDoNothing() = runBlocking {
    // GIVEN
    val predicate = StorePredicate(read = true)
    val storePage = givenPageStore(predicate, defaultEdgeArraySize, forceProperty = ForceProperty.Unread)
    val store = createNotificationStore(
      predicate, Result.success(storePage)
    )

    // WHEN
    store.fetch().getOrThrow()
    val initialCounts = InitialNotificationStoreCounts(store)
    val chosenIndex = anyIndexForDefaultEdgeArraySize
    val markReadNotification = store[chosenIndex]
    coEvery { actionNotificationInteractor.invoke(MARK_AS_UNREAD, markReadNotification.id, userQuery) } returns Unit
    store.markAsUnread(store[chosenIndex]).getOrThrow()

    // THEN
    store[chosenIndex].readAt.shouldBeNull()
    store.totalCount.shouldBeExactly(initialCounts.totalCount)
    store.unreadCount.shouldBeExactly(initialCounts.unreadCount)
    store.unseenCount.shouldBeExactly(initialCounts.unseenCount)
    Unit
  }

  @Test
  fun test_markNotificationAsArchive_withDefaultStorePredicate_shouldCallActioNotificationInteractor() = runBlocking {
    // GIVEN
    val predicate = StorePredicate()
    val storePage = givenPageStore(predicate, defaultEdgeArraySize, forceProperty = ForceProperty.Read)
    val store = createNotificationStore(
      predicate, Result.success(storePage)
    )

    // WHEN
    store.fetch().getOrThrow()
    val initialCounts = InitialNotificationStoreCounts(store)
    val chosenIndex = anyIndexForDefaultEdgeArraySize
    val markReadNotification = store[chosenIndex]
    coEvery { actionNotificationInteractor.invoke(ARCHIVE, markReadNotification.id, userQuery) } returns Unit
    store.archive(store[chosenIndex]).getOrThrow()

    // THEN
    coVerify(exactly = 1, timeout = 1000) { fetchStorePageInteractor.invoke(any(), any(), any()) }
    coVerify { actionNotificationInteractor.invoke(ARCHIVE, markReadNotification.id, userQuery) }
    confirmVerified(actionNotificationInteractor)
  }

  @Test
  fun test_markNotificationAsArchive_withDefaultStorePredicate_shouldHaveArchiveDate() = runBlocking {
    // GIVEN
    val predicate = StorePredicate()
    val storePage = givenPageStore(predicate, defaultEdgeArraySize, forceProperty = ForceProperty.Read)
    val store = createNotificationStore(
      predicate, Result.success(storePage)
    )

    // WHEN
    store.fetch().getOrThrow()
    val initialCounts = InitialNotificationStoreCounts(store)
    val chosenIndex = anyIndexForDefaultEdgeArraySize
    val markReadNotification = store[chosenIndex]
    coEvery { actionNotificationInteractor.invoke(ARCHIVE, markReadNotification.id, userQuery) } returns Unit
    store.archive(store[chosenIndex]).getOrThrow()

    //THEN
    store[chosenIndex].archivedAt.shouldNotBeNull()
    Unit
  }

  @Test
  fun test_markNotificationAsUnarchive_withDefaultStorePredicate_shouldCallActioNotificationInteractor() = runBlocking {
    // GIVEN
    val predicate = StorePredicate()
    val storePage = givenPageStore(predicate, defaultEdgeArraySize)
    val store = createNotificationStore(
      predicate, Result.success(storePage)
    )

    // WHEN
    store.fetch().getOrThrow()
    val initialCounts = InitialNotificationStoreCounts(store)
    val chosenIndex = anyIndexForDefaultEdgeArraySize
    val markReadNotification = store[chosenIndex]
    coEvery { actionNotificationInteractor.invoke(UNARCHIVE, markReadNotification.id, userQuery) } returns Unit
    store.unarchive(store[chosenIndex]).getOrThrow()

    // THEN
    coVerify(exactly = 1, timeout = 1000) { fetchStorePageInteractor.invoke(any(), any(), any()) }
    coVerify { actionNotificationInteractor.invoke(UNARCHIVE, markReadNotification.id, userQuery) }
    confirmVerified(actionNotificationInteractor)
  }

  @Test
  fun test_markNotificationUnarchive_withDefaultStorePredicate_shouldHaveNilArchiveDate() = runBlocking {
    // GIVEN
    val predicate = StorePredicate()
    val storePage = givenPageStore(predicate, defaultEdgeArraySize, forceProperty = ForceProperty.Read)
    val store = createNotificationStore(
      predicate, Result.success(storePage)
    )

    // WHEN
    store.fetch().getOrThrow()
    val initialCounts = InitialNotificationStoreCounts(store)
    val chosenIndex = anyIndexForDefaultEdgeArraySize
    val markReadNotification = store[chosenIndex]
    coEvery { actionNotificationInteractor.invoke(UNARCHIVE, markReadNotification.id, userQuery) } returns Unit
    store.unarchive(store[chosenIndex]).getOrThrow()

    //THEN
    store[chosenIndex].archivedAt.shouldBeNull()
  }

  @Test
  fun test_markNotificationAllRead_withDefaultStorePredicate_shouldCallActioNotificationInteractor() = runBlocking {
    // GIVEN
    val predicate = StorePredicate()
    val storePage = givenPageStore(predicate, defaultEdgeArraySize)
    val store = createNotificationStore(
      predicate, Result.success(storePage)
    )

    // WHEN
    store.fetch().getOrThrow()
    anyIndexForDefaultEdgeArraySize
    coEvery { actionNotificationInteractor.invoke(MARK_ALL_AS_READ, null, userQuery) } returns Unit
    store.markAllNotificationAsRead().getOrThrow()

    // THEN
    coVerify(exactly = 1, timeout = 1000) { fetchStorePageInteractor.invoke(any(), any(), any()) }
    coVerify { actionNotificationInteractor.invoke(MARK_ALL_AS_READ, null, userQuery) }
    confirmVerified(actionNotificationInteractor)
  }

  @Test
  fun test_markNotificationAllRead_withError_shouldReturnError() = runBlocking {
    // GIVEN
    val predicate = StorePredicate()
    val storePage = givenPageStore(predicate, defaultEdgeArraySize)
    val store = createNotificationStore(
      predicate, Result.success(storePage)
    )

    // WHEN
    store.fetch().getOrThrow()
    anyIndexForDefaultEdgeArraySize
    coEvery { actionNotificationInteractor.invoke(MARK_ALL_AS_READ, null, userQuery) } throws MagicBellError("Error")
    var errorExpected: Exception? = null
    try {
      store.markAllNotificationAsRead().getOrThrow()
    } catch (e: Exception) {
      errorExpected = e
    }

    // THEN
    coVerify(exactly = 1, timeout = 1000) { fetchStorePageInteractor.invoke(any(), any(), any()) }
    coVerify { actionNotificationInteractor.invoke(MARK_ALL_AS_READ, null, userQuery) }
    confirmVerified(actionNotificationInteractor)
    errorExpected.shouldNotBeNull()
    Unit
  }

  @Test
  fun test_markAllNotificationAsRead_withDefaultStorePredicate_shouldMarkAllNotificationWithReadDate() = runBlocking {
    // GIVEN
    val predicate = StorePredicate()
    val storePage = givenPageStore(predicate, defaultEdgeArraySize)
    val store = createNotificationStore(
      predicate, Result.success(storePage)
    )

    // WHEN
    store.fetch().getOrThrow()
    val initialCounts = InitialNotificationStoreCounts(store)
    anyIndexForDefaultEdgeArraySize
    coEvery { actionNotificationInteractor.invoke(MARK_ALL_AS_READ, null, userQuery) } returns Unit
    store.markAllNotificationAsRead().getOrThrow()

    // THEN
    store.forEach {
      it.readAt.shouldNotBeNull()
      it.seenAt.shouldNotBeNull()
    }
    store.totalCount.shouldBeExactly(initialCounts.totalCount)
    store.unreadCount.shouldBeExactly(0)
    store.unseenCount.shouldBeExactly(0)
    Unit
  }

  @Test
  fun test_markAllNotificationAsRead_withUnreadStorePredicate_shouldClearNotifications() = runBlocking {
    // GIVEN
    val predicate = StorePredicate(read = false)
    val storePage = givenPageStore(predicate, defaultEdgeArraySize, forceProperty = ForceProperty.Unread)
    val store = createNotificationStore(
      predicate, Result.success(storePage)
    )

    // WHEN
    store.fetch().getOrThrow()
    val initialCounts = InitialNotificationStoreCounts(store)
    anyIndexForDefaultEdgeArraySize
    coEvery { actionNotificationInteractor.invoke(MARK_ALL_AS_READ, null, userQuery) } returns Unit
    store.markAllNotificationAsRead().getOrThrow()

    // THEN
    store.forEach {
      it.readAt.shouldNotBeNull()
      it.seenAt.shouldNotBeNull()
    }
    store.totalCount.shouldNotBe(initialCounts.totalCount)
    store.unreadCount.shouldNotBe(initialCounts.unreadCount)
    store.unseenCount.shouldNotBe(initialCounts.unseenCount)
    Unit
  }

  @Test
  fun test_markAllNotificationAsRead_withReadStorePredicate_shouldBeAllTheSame() = runBlocking {
    // GIVEN
    val predicate = StorePredicate(read = true)
    val storePage = givenPageStore(predicate, defaultEdgeArraySize, forceProperty = ForceProperty.Read)
    val store = createNotificationStore(
      predicate, Result.success(storePage)
    )

    // WHEN
    store.fetch().getOrThrow()
    val initialCounts = InitialNotificationStoreCounts(store)
    anyIndexForDefaultEdgeArraySize
    coEvery { actionNotificationInteractor.invoke(MARK_ALL_AS_READ, null, userQuery) } returns Unit
    store.markAllNotificationAsRead().getOrThrow()

    // THEN
    store.forEach {
      it.readAt.shouldNotBeNull()
      it.seenAt.shouldNotBeNull()
    }
    store.totalCount.shouldBeExactly(initialCounts.totalCount)
    store.unreadCount.shouldBeExactly(initialCounts.unreadCount)
    store.unseenCount.shouldBeExactly(initialCounts.unseenCount)
    Unit
  }

  @Test
  fun test_markAllNotificationSeen_withDefaultStorePredicate_shouldCallActioNotificationInteractor() = runBlocking {
    // GIVEN
    val predicate = StorePredicate()
    val storePage = givenPageStore(predicate, defaultEdgeArraySize)
    val store = createNotificationStore(
      predicate, Result.success(storePage)
    )

    // WHEN
    store.fetch().getOrThrow()
    val initialCounts = InitialNotificationStoreCounts(store)
    anyIndexForDefaultEdgeArraySize
    coEvery { actionNotificationInteractor.invoke(MARK_ALL_AS_SEEN, null, userQuery) } returns Unit
    store.markAllNotificationAsSeen().getOrThrow()

    // THEN
    coVerify(exactly = 1, timeout = 1000) { fetchStorePageInteractor.invoke(any(), any(), any()) }
    coVerify { actionNotificationInteractor.invoke(MARK_ALL_AS_SEEN, null, userQuery) }
    confirmVerified(actionNotificationInteractor)
  }

  @Test
  fun test_markAllNotificationAsSeen_withDefaultStorePredicate_shouldMarkAllNotificationWithSeenDate() = runBlocking {
    // GIVEN
    val predicate = StorePredicate()
    val storePage = givenPageStore(predicate, defaultEdgeArraySize)
    val store = createNotificationStore(
      predicate, Result.success(storePage)
    )

    // WHEN
    store.fetch().getOrThrow()
    val initialCounts = InitialNotificationStoreCounts(store)
    anyIndexForDefaultEdgeArraySize
    coEvery { actionNotificationInteractor.invoke(MARK_ALL_AS_SEEN, null, userQuery) } returns Unit
    store.markAllNotificationAsSeen().getOrThrow()

    // THEN
    store.forEach {
      it.seenAt.shouldNotBeNull()
    }
    store.totalCount.shouldBeExactly(initialCounts.totalCount)
    store.unreadCount.shouldBeExactly(initialCounts.unreadCount)
    store.unseenCount.shouldBeExactly(0)
    Unit
  }

  @Test
  fun test_markAllNotificationAsSeen_withUnreadStorePredicate_shouldMarkAllNotificationWithSeenDate() = runBlocking {
    // GIVEN
    val predicate = StorePredicate(read = false)
    val storePage = givenPageStore(predicate, defaultEdgeArraySize, forceProperty = ForceProperty.Unread)
    val store = createNotificationStore(
      predicate, Result.success(storePage)
    )

    // WHEN
    store.fetch().getOrThrow()
    val initialCounts = InitialNotificationStoreCounts(store)
    anyIndexForDefaultEdgeArraySize
    coEvery { actionNotificationInteractor.invoke(MARK_ALL_AS_SEEN, null, userQuery) } returns Unit
    store.markAllNotificationAsSeen().getOrThrow()

    // THEN
    store.totalCount.shouldBeExactly(initialCounts.totalCount)
    store.unreadCount.shouldBeExactly(initialCounts.unreadCount)
    store.unseenCount.shouldNotBe(initialCounts.unseenCount)
    Unit
  }

  @Test
  fun test_markAllNotificationAsSeen_withReadStorePredicate_shouldMarkAllNotificationWithSeenDate() = runBlocking {
    // GIVEN
    val predicate = StorePredicate(read = true)
    val storePage = givenPageStore(predicate, defaultEdgeArraySize, forceProperty = ForceProperty.Read)
    val store = createNotificationStore(
      predicate, Result.success(storePage)
    )

    // WHEN
    store.fetch().getOrThrow()
    val initialCounts = InitialNotificationStoreCounts(store)
    anyIndexForDefaultEdgeArraySize
    coEvery { actionNotificationInteractor.invoke(MARK_ALL_AS_SEEN, null, userQuery) } returns Unit
    store.markAllNotificationAsSeen().getOrThrow()

    store.totalCount.shouldBeExactly(initialCounts.totalCount)
    store.unreadCount.shouldBeExactly(initialCounts.unreadCount)
    store.unseenCount.shouldBeExactly(initialCounts.unseenCount)
    Unit
  }

  @Test
  fun test_notifyInsertNotifications_withDefaultStorePredicate_ShouldNotifyInsertIndexesArray() = runBlocking {
    // GIVEN
    val contentObserver = ContentObserverMock()
    val predicate = StorePredicate()
    val storePage = StoreMotherObject.createStorePage(
      anyNotificationEdgeArray(predicate, defaultEdgeArraySize, ForceProperty.None),
      PageInfoMotherObject.createPageInfo(AnyCursor.ANY.value, true)
    )
    val store = createNotificationStore(predicate, Result.success(storePage))
    store.addContentObserver(contentObserver)

    // WHEN
    store.fetch().getOrThrow()

    // THEN
    var indexes = 0.until(store.size).toList()
    val sizeIndexes = indexes.size
    contentObserver.didInsertCounter.shouldBeExactly(1)
    contentObserver.didInsertSpy[0].indexes.shouldBe(indexes)
    coVerify(exactly = 1, timeout = 1000) { fetchStorePageInteractor.invoke(any(), any(), any()) }
    store.size.shouldBeExactly(defaultEdgeArraySize)

    store.fetch().getOrThrow()
    coVerify(exactly = 2, timeout = 1000) { fetchStorePageInteractor.invoke(any(), any(), any()) }
    store.size.shouldBeExactly(defaultEdgeArraySize * 2)
    indexes = sizeIndexes.until(store.size).toList()
    contentObserver.didInsertCounter.shouldBeExactly(2)
    contentObserver.didInsertSpy[1].indexes.shouldBe(indexes)
  }

  @Test
  fun test_notifyDeleteNotification_WithDefaultStorePredicate_ShouldNotifyCounters() = runBlocking {
    // GIVEN
    val contentObserver = ContentObserverMock()
    val predicate = StorePredicate()
    val storePage = StoreMotherObject.createStorePage(
      anyNotificationEdgeArray(predicate, defaultEdgeArraySize, ForceProperty.None),
      PageInfoMotherObject.createPageInfo(AnyCursor.ANY.value, true)
    )
    val store = createNotificationStore(predicate, Result.success(storePage))
    store.addContentObserver(contentObserver)

    // WHEN
    store.fetch().getOrThrow()
    val removeIndex = anyIndexForDefaultEdgeArraySize
    coEvery { deleteNotificationInteractor.invoke(store[removeIndex].id, userQuery) } returns Unit
    store.delete(store[removeIndex]).getOrThrow()

    // THEN
    contentObserver.didDeleteCounter.shouldBeExactly(1)
    contentObserver.didDeleteSpy[0].indexes.shouldBe(listOf(removeIndex))
  }
}