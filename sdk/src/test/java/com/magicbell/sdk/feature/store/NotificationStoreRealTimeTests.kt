package com.magicbell.sdk.feature.store

import com.magicbell.sdk.common.query.UserQuery
import com.magicbell.sdk.common.threading.MainThread
import com.magicbell.sdk.feature.config.Config
import com.magicbell.sdk.feature.config.Ws
import com.magicbell.sdk.feature.config.interactor.DeleteConfigInteractor
import com.magicbell.sdk.feature.config.interactor.GetConfigInteractor
import com.magicbell.sdk.feature.notification.interactor.ActionNotificationInteractor
import com.magicbell.sdk.feature.notification.interactor.DeleteNotificationInteractor
import com.magicbell.sdk.feature.notification.motherobject.ForceProperty
import com.magicbell.sdk.feature.store.interactor.FetchStorePageInteractor
import com.magicbell.sdk.feature.store.motherobject.givenPageStore
import com.magicbell.sdk.feature.store.utils.ContentObserverMock
import com.magicbell.sdk.feature.store.utils.CountObserverMock
import com.magicbell.sdk.feature.store.utils.InitialNotificationStoreCounts
import com.magicbell.sdk.feature.storerealtime.mock.RealTimeEventMock
import com.magicbell.sdk.feature.storerealtime.mock.StoreRealTimeMock
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.concurrent.Executors
import kotlin.random.Random

internal class NotificationStoreRealTimeTests {
  private val defaultEdgeArraySize = 20
  private val anyIndexForDefaultEdgeArraySize by lazy { Random.nextInt(0, defaultEdgeArraySize) }

  private val userQuery = UserQuery.createEmail("javier@mobilejazz.com")
  val coroutineContext = Executors.newFixedThreadPool(1).asCoroutineDispatcher()
  private val mainThread = object : MainThread {
    override fun post(run: () -> Unit) {
      run()
    }
  }

  lateinit var storeRealTime: StoreRealTimeMock
  var fetchStorePageInteractor: FetchStorePageInteractor = mockk()
  var actionNotificationInteractor: ActionNotificationInteractor = mockk()
  var deleteNotificationInteractor: DeleteNotificationInteractor = mockk()
  var getConfigInteractor: GetConfigInteractor = mockk()
  var deleteConfigInteractor: DeleteConfigInteractor = mockk()

  lateinit var storeDirector: StoreDirector

  fun createStoreDirector(
    predicate: StorePredicate,
    fetchStoreExpectedResult: Result<StorePage>
  ): NotificationStore {

    fetchStoreExpectedResult.fold(onSuccess = {
      coEvery { fetchStorePageInteractor.invoke(any(), any(), any()) } returns fetchStoreExpectedResult.getOrThrow()
    }, onFailure = {
      coEvery { fetchStorePageInteractor.invoke(any(), any(), any()) } throws it
    })

    coEvery { getConfigInteractor.invoke(any(), any()) } returns Config(Ws("channel-1"))
    coEvery { deleteConfigInteractor.invoke(any()) } returns Unit

    storeRealTime = StoreRealTimeMock()
    storeDirector = RealTimeByPredicateStoreDirector(
      userQuery,
      coroutineContext,
      mainThread,
      fetchStorePageInteractor,
      actionNotificationInteractor,
      deleteNotificationInteractor,
      getConfigInteractor,
      deleteConfigInteractor,
      storeRealTime
    )

    return storeDirector.with(predicate)
  }

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

  @Test
  fun test_addRealTimeStore() {
    // GIVEN
    val predicate = StorePredicate()
    val storePage = givenPageStore(predicate, defaultEdgeArraySize)

    // WHEN
    createStoreDirector(
      predicate,
      Result.success(storePage)
    )

    // THEN
    storeRealTime.observers.size.shouldBeExactly(1)
  }

  @Test
  fun test_notifyNewNotification_withDefaultStorePredicate_shouldRefreshStore() = runBlocking {
    // GIVEN
    val predicate = StorePredicate()
    val storePage = givenPageStore(predicate, defaultEdgeArraySize)
    val store = createStoreDirector(
      predicate,
      Result.success(storePage)
    )

    // WHEN
    store.fetch().getOrThrow()
    storeRealTime.processMessage(RealTimeEventMock.NewNotification("NewNotification"))

    // THEN
    coVerify(exactly = 2) { fetchStorePageInteractor.invoke(any(), any(), any()) }
    store.size.shouldBeExactly(defaultEdgeArraySize)
    storePage.edges.mapIndexed { index, edge ->
      store[index].id.shouldBe(edge.node.id)
    }
    Unit
  }

  @Test
  fun test_notifyReadNotification_withDefaultStorePredicateAndReadAndExists_shouldDoNothing() = runBlocking {
    // GIVEN
    val predicate = StorePredicate()
    val storePage = givenPageStore(predicate, defaultEdgeArraySize, forceProperty = ForceProperty.Read)
    val store = createStoreDirector(
      predicate,
      Result.success(storePage)
    )

    // WHEN
    store.fetch().getOrThrow()
    val initialCounter = InitialNotificationStoreCounts(store)
    val chosenIndex = anyIndexForDefaultEdgeArraySize
    storeRealTime.processMessage(RealTimeEventMock.ReadNotification(chosenIndex.toString()))

    // THEN
    coVerify(exactly = 1) { fetchStorePageInteractor.invoke(any(), any(), any()) }
    store.size.shouldBeExactly(defaultEdgeArraySize)
    store.totalCount.shouldBeExactly(initialCounter.totalCount)
    store.unreadCount.shouldBeExactly(initialCounter.unreadCount)
    Unit
  }

  @Test
  fun test_notifyReadNotification_withDefaultStorePredicateAndUnreadAndExists_shouldUpdateNotification() = runBlocking {
    // GIVEN
    val predicate = StorePredicate()
    val storePage = givenPageStore(predicate, defaultEdgeArraySize, forceProperty = ForceProperty.Unread)
    val store = createStoreDirector(
      predicate,
      Result.success(storePage)
    )

    // WHEN
    store.fetch().getOrThrow()
    val initialCounter = InitialNotificationStoreCounts(store)
    val chosenIndex = anyIndexForDefaultEdgeArraySize
    storeRealTime.processMessage(RealTimeEventMock.ReadNotification(chosenIndex.toString()))

    // THEN
    store[chosenIndex].readAt.shouldNotBeNull()
    coVerify(exactly = 1) { fetchStorePageInteractor.invoke(any(), any(), any()) }
    store.size.shouldBeExactly(defaultEdgeArraySize)
    store.totalCount.shouldBeExactly(initialCounter.totalCount)
    store.unreadCount.shouldBeExactly(initialCounter.unreadCount - 1)
    Unit
  }

  @Test
  fun test_notifyReadNotification_withDefaultStorePredicateAndUnseenAndExists_shouldUpdateNotification() = runBlocking {
    // GIVEN
    val predicate = StorePredicate()
    val storePage = givenPageStore(predicate, defaultEdgeArraySize, forceProperty = ForceProperty.Unseen)
    val store = createStoreDirector(
      predicate,
      Result.success(storePage)
    )

    // WHEN
    store.fetch().getOrThrow()
    val initialCounter = InitialNotificationStoreCounts(store)
    val chosenIndex = anyIndexForDefaultEdgeArraySize
    storeRealTime.processMessage(RealTimeEventMock.ReadNotification(chosenIndex.toString()))

    // THEN
    store[chosenIndex].readAt.shouldNotBeNull()
    store[chosenIndex].seenAt.shouldNotBeNull()
    coVerify(exactly = 1) { fetchStorePageInteractor.invoke(any(), any(), any()) }
    store.size.shouldBeExactly(defaultEdgeArraySize)
    store.totalCount.shouldBeExactly(initialCounter.totalCount)
    store.unreadCount.shouldBeExactly(initialCounter.unreadCount - 1)
    store.unseenCount.shouldBeExactly(initialCounter.unseenCount - 1)
    Unit
  }

  @Test
  fun test_notifyReadNotification_withDefaultStorePredicateAndDoesntExists_shouldRefresh() = runBlocking {
    // GIVEN
    val predicate = StorePredicate()
    val storePage = givenPageStore(predicate, defaultEdgeArraySize)
    val store = createStoreDirector(
      predicate,
      Result.success(storePage)
    )

    // WHEN
    store.fetch().getOrThrow()
    val initialCounter = InitialNotificationStoreCounts(store)
    storeRealTime.processMessage(RealTimeEventMock.ReadNotification("Not exists"))

    // THEN
    coVerify(exactly = 2) { fetchStorePageInteractor.invoke(any(), any(), any()) }
    store.size.shouldBeExactly(defaultEdgeArraySize)
    store.totalCount.shouldBeExactly(initialCounter.totalCount)
    storePage.edges.mapIndexed { index, edge ->
      store[index].id.shouldBe(edge.node.id)
    }
    Unit
  }

  @Test
  fun test_notifyUnreadNotification_withDefaultStorePredicateAndReadAndExists_shouldUpdateNotification() = runBlocking {
    // GIVEN
    val predicate = StorePredicate()
    val storePage = givenPageStore(predicate, defaultEdgeArraySize, forceProperty = ForceProperty.Read)
    val store = createStoreDirector(
      predicate,
      Result.success(storePage)
    )

    // WHEN
    store.fetch().getOrThrow()
    val initialCounter = InitialNotificationStoreCounts(store)
    val chosenIndex = anyIndexForDefaultEdgeArraySize
    storeRealTime.processMessage(RealTimeEventMock.UnreadNotification(chosenIndex.toString()))

    // THEN
    store[chosenIndex].readAt.shouldBeNull()
    coVerify(exactly = 1) { fetchStorePageInteractor.invoke(any(), any(), any()) }
    store.size.shouldBeExactly(defaultEdgeArraySize)
    store.totalCount.shouldBeExactly(initialCounter.totalCount)
    store.unreadCount.shouldBeExactly(initialCounter.unreadCount + 1)
    store.unseenCount.shouldBeExactly(initialCounter.unseenCount)
    Unit
  }

  @Test
  fun test_notifyUnreadNotification_withDefaultStorePredicateAndUnreadAndExists_shouldUpdateNotification() = runBlocking {
    // GIVEN
    val predicate = StorePredicate()
    val storePage = givenPageStore(predicate, defaultEdgeArraySize, forceProperty = ForceProperty.Unread)
    val store = createStoreDirector(
      predicate,
      Result.success(storePage)
    )

    // WHEN
    store.fetch().getOrThrow()
    val initialCounter = InitialNotificationStoreCounts(store)
    val chosenIndex = anyIndexForDefaultEdgeArraySize
    storeRealTime.processMessage(RealTimeEventMock.UnreadNotification(chosenIndex.toString()))

    // THEN
    store[chosenIndex].readAt.shouldBeNull()
    coVerify(exactly = 1) { fetchStorePageInteractor.invoke(any(), any(), any()) }
    store.size.shouldBeExactly(defaultEdgeArraySize)
    store.totalCount.shouldBeExactly(initialCounter.totalCount)
    store.unreadCount.shouldBeExactly(initialCounter.unreadCount)
    store.unseenCount.shouldBeExactly(initialCounter.unseenCount)
    Unit
  }

  @Test
  fun test_notifyUnreadNotification_withDefaultStorePredicateAndNotExists_shouldRefresh() = runBlocking {
    // GIVEN
    val predicate = StorePredicate()
    val storePage = givenPageStore(predicate, defaultEdgeArraySize)
    val store = createStoreDirector(
      predicate,
      Result.success(storePage)
    )

    // WHEN
    store.fetch().getOrThrow()
    storeRealTime.processMessage(RealTimeEventMock.UnreadNotification("Not exists"))

    // THEN
    coVerify(exactly = 2) { fetchStorePageInteractor.invoke(any(), any(), any()) }
    store.size.shouldBeExactly(defaultEdgeArraySize)
    Unit
  }

  @Test
  fun test_notifyDeleteNotification_withDefaultStorePredicateAndUnreadAndExists_shouldRemoveNotificationAndUnreadCount() = runBlocking {
    // GIVEN
    val predicate = StorePredicate()
    val storePage = givenPageStore(predicate, defaultEdgeArraySize, forceProperty = ForceProperty.Unread)
    val store = createStoreDirector(
      predicate,
      Result.success(storePage)
    )

    // WHEN
    store.fetch().getOrThrow()
    val initialCounter = InitialNotificationStoreCounts(store)
    val chosenIndex = anyIndexForDefaultEdgeArraySize
    val removedNotificationId = store[chosenIndex].id
    storeRealTime.processMessage(RealTimeEventMock.DeleteNotification(chosenIndex.toString()))

    // THEN
    coVerify(exactly = 1) { fetchStorePageInteractor.invoke(any(), any(), any()) }
    store.totalCount.shouldBeExactly(initialCounter.totalCount - 1)
    store.unreadCount.shouldBeExactly(initialCounter.unreadCount - 1)
    store.forEach { notification ->
      notification.id.shouldNotBe(removedNotificationId)
    }
  }

  @Test
  fun test_notifyDeleteNotification_withDefaultStorePredicateAndUnseenAndExists_shouldUpdateUnseenCount() = runBlocking {
    // GIVEN
    val predicate = StorePredicate()
    val storePage = givenPageStore(predicate, defaultEdgeArraySize, forceProperty = ForceProperty.Unseen)
    val store = createStoreDirector(
      predicate,
      Result.success(storePage)
    )

    // WHEN
    store.fetch().getOrThrow()
    val initialCounter = InitialNotificationStoreCounts(store)
    val chosenIndex = anyIndexForDefaultEdgeArraySize
    storeRealTime.processMessage(RealTimeEventMock.DeleteNotification(chosenIndex.toString()))

    // THEN
    coVerify(exactly = 1) { fetchStorePageInteractor.invoke(any(), any(), any()) }
    store.unseenCount.shouldBeExactly(initialCounter.unseenCount - 1)
    Unit
  }

  @Test
  fun test_notifyDeleteNotification_withDefaultStorePredicateAndSeenAndExists_shouldSameUnseenCount() = runBlocking {
    // GIVEN
    val predicate = StorePredicate()
    val storePage = givenPageStore(predicate, defaultEdgeArraySize, forceProperty = ForceProperty.Seen)
    val store = createStoreDirector(
      predicate,
      Result.success(storePage)
    )

    // WHEN
    store.fetch().getOrThrow()
    val initialCounter = InitialNotificationStoreCounts(store)
    val chosenIndex = anyIndexForDefaultEdgeArraySize
    storeRealTime.processMessage(RealTimeEventMock.DeleteNotification(chosenIndex.toString()))

    // THEN
    coVerify(exactly = 1) { fetchStorePageInteractor.invoke(any(), any(), any()) }
    store.unseenCount.shouldBeExactly(initialCounter.unseenCount)
    Unit
  }

  @Test
  fun test_notifyDeleteNotification_withDefaultStorePredicateAndNotExists_shouldDoNothing() = runBlocking {
    // GIVEN
    val predicate = StorePredicate()
    val storePage = givenPageStore(predicate, defaultEdgeArraySize)
    val store = createStoreDirector(
      predicate,
      Result.success(storePage)
    )

    // WHEN
    store.fetch().getOrThrow()
    val initialCounter = InitialNotificationStoreCounts(store)
    storeRealTime.processMessage(RealTimeEventMock.DeleteNotification("Not exists"))

    // THEN
    coVerify(exactly = 1) { fetchStorePageInteractor.invoke(any(), any(), any()) }
    store.totalCount.shouldBeExactly(initialCounter.totalCount)
    store.unreadCount.shouldBeExactly(initialCounter.unreadCount)
    store.unseenCount.shouldBeExactly(initialCounter.unseenCount)
    Unit
  }

  @Test
  fun test_notifyReadAllNotification_withDefaultStorePredicate_shouldRefresh() = runBlocking {
    // GIVEN
    val predicate = StorePredicate()
    val storePage = givenPageStore(predicate, defaultEdgeArraySize, forceProperty = ForceProperty.Read)
    val store = createStoreDirector(
      predicate,
      Result.success(storePage)
    )

    // WHEN
    store.fetch().getOrThrow()
    storeRealTime.processMessage(RealTimeEventMock.ReadAllNotification)

    // THEN
    coVerify(exactly = 2) { fetchStorePageInteractor.invoke(any(), any(), any()) }
    store.size.shouldBeExactly(defaultEdgeArraySize)
    store.forEach { notification ->
      notification.readAt.shouldNotBeNull()
    }
  }

  @Test
  fun test_notifySeenAllNotification_withDefaultStorePredicate_shouldRefresh() = runBlocking {
    // GIVEN
    val predicate = StorePredicate()
    val storePage = givenPageStore(predicate, defaultEdgeArraySize, forceProperty = ForceProperty.Read)
    val store = createStoreDirector(
      predicate,
      Result.success(storePage)
    )

    // WHEN
    store.fetch().getOrThrow()
    storeRealTime.processMessage(RealTimeEventMock.SeenAllNotification)

    // THEN
    coVerify(exactly = 2) { fetchStorePageInteractor.invoke(any(), any(), any()) }
    store.size.shouldBeExactly(defaultEdgeArraySize)
    store.forEach { notification ->
      notification.seenAt.shouldNotBeNull()
    }
  }

  // MARK: - Observer tests

  @Test
  fun test_addContentObserver_ShouldNotifyRefreshStore() = runBlocking {
    // GIVEN
    val contentObserver = ContentObserverMock()
    val predicate = StorePredicate()
    val storePage = givenPageStore(predicate, defaultEdgeArraySize)
    val store = createStoreDirector(
      predicate,
      Result.success(storePage)
    )
    store.addContentObserver(contentObserver)

    // WHEN
    storeRealTime.processMessage(RealTimeEventMock.NewNotification("NewNotification"))

    // THEN
    delay(100)
    contentObserver.reloadStoreCounter.shouldBeExactly(1)
    contentObserver.reloadStoreSpy.shouldNotBeEmpty()
    Unit
  }

  @Test
  fun test_notifyReadNotification_withReadStorePredicateAndExists_ShouldDidChangeDelegate() = runBlocking {
    // GIVEN
    val contentObserver = ContentObserverMock()
    val predicate = StorePredicate(read = true)
    val storePage = givenPageStore(predicate, defaultEdgeArraySize, forceProperty = ForceProperty.Read)
    val store = createStoreDirector(
      predicate,
      Result.success(storePage)
    )
    store.addContentObserver(contentObserver)

    // WHEN
    store.fetch().getOrThrow()
    val chosenIndex = anyIndexForDefaultEdgeArraySize
    storeRealTime.processMessage(RealTimeEventMock.ReadNotification(chosenIndex.toString()))

    // THEN
    contentObserver.reloadStoreCounter.shouldBeExactly(0)
    contentObserver.didChangeCounter.shouldBeExactly(1)
    contentObserver.didChangeSpy[0].indexes.shouldBe(listOf(chosenIndex))
  }

  @Test
  fun test_notifyReadNotification_withReadStorePredicateAndDoesntExist_ShouldNotifyReadNotification() = runBlocking {
    // GIVEN
    val contentObserver = ContentObserverMock()
    val predicate = StorePredicate(read = true)
    val storePage = givenPageStore(predicate, defaultEdgeArraySize, forceProperty = ForceProperty.Read)
    val store = createStoreDirector(
      predicate,
      Result.success(storePage)
    )
    store.addContentObserver(contentObserver)

    // WHEN
    store.fetch().getOrThrow()
    storeRealTime.processMessage(RealTimeEventMock.ReadNotification("Not exists"))

    // THEN
    delay(100)
    contentObserver.reloadStoreCounter.shouldBeExactly(1)
    contentObserver.didChangeCounter.shouldBeExactly(0)
    Unit
  }

  @Test
  fun test_notifyReadNotification_WithUnreadStorePredicateAndExists_ShouldNotifyChange() = runBlocking {
    // GIVEN
    val contentObserver = ContentObserverMock()
    val predicate = StorePredicate(read = false)
    val storePage = givenPageStore(predicate, defaultEdgeArraySize, forceProperty = ForceProperty.Unread)
    val store = createStoreDirector(
      predicate,
      Result.success(storePage)
    )
    store.addContentObserver(contentObserver)

    // WHEN
    store.fetch().getOrThrow()
    val chosenIndex = anyIndexForDefaultEdgeArraySize
    storeRealTime.processMessage(RealTimeEventMock.ReadNotification(chosenIndex.toString()))


    // THEN
    contentObserver.reloadStoreCounter.shouldBeExactly(0)
    contentObserver.didChangeCounter.shouldBeExactly(0)
    contentObserver.didDeleteCounter.shouldBeExactly(1)
    contentObserver.didDeleteSpy[0].indexes.shouldBe(listOf(chosenIndex))
  }

  @Test
  fun test_notifyDeleteNotification_WithDefaultStorePredicateAndExists_ShouldNotifyDeletion() = runBlocking {
    // GIVEN
    val contentObserver = ContentObserverMock()
    val predicate = StorePredicate()
    val storePage = givenPageStore(predicate, defaultEdgeArraySize)
    val store = createStoreDirector(
      predicate,
      Result.success(storePage)
    )
    store.addContentObserver(contentObserver)

    // WHEN
    store.fetch().getOrThrow()
    val chosenIndex = anyIndexForDefaultEdgeArraySize
    storeRealTime.processMessage(RealTimeEventMock.DeleteNotification(chosenIndex.toString()))

    // THEN
    contentObserver.reloadStoreCounter.shouldBeExactly(0)
    contentObserver.didChangeCounter.shouldBeExactly(0)
    contentObserver.didDeleteCounter.shouldBeExactly(1)
    contentObserver.didDeleteSpy[0].indexes.shouldBe(listOf(chosenIndex))
  }

  @Test
  fun test_notifyMarkAllRead_WithUnreadStorePredicate_ShouldClearStore() = runBlocking {
    // GIVEN
    val contentObserver = ContentObserverMock()
    val predicate = StorePredicate(read = false)
    val storePage = givenPageStore(predicate, defaultEdgeArraySize, forceProperty = ForceProperty.Unread)
    val store = createStoreDirector(
      predicate,
      Result.success(storePage)
    )
    store.addContentObserver(contentObserver)

    // WHEN
    store.fetch().getOrThrow()
    val initialCounts = InitialNotificationStoreCounts(store)
    storeRealTime.processMessage(RealTimeEventMock.ReadAllNotification)

    // THEN
    contentObserver.reloadStoreCounter.shouldBeExactly(0)
    contentObserver.didChangeCounter.shouldBeExactly(0)
    contentObserver.didDeleteCounter.shouldBeExactly(1)
    contentObserver.didDeleteSpy[0].indexes.shouldBe(0.until(initialCounts.totalCount).toList())
    store.totalCount.shouldBeExactly(0)
    store.unreadCount.shouldBeExactly(0)
    store.unseenCount.shouldBeExactly(0)
    Unit
  }

  @Test
  fun test_notifyMarkAllSeen_WithUnseenStorePredicate_ShouldClearStore() = runBlocking {
    // GIVEN
    val contentObserver = ContentObserverMock()
    val predicate = StorePredicate(seen = false)
    val storePage = givenPageStore(predicate, defaultEdgeArraySize, forceProperty = ForceProperty.Unseen)
    val store = createStoreDirector(
      predicate,
      Result.success(storePage)
    )
    store.addContentObserver(contentObserver)

    // WHEN
    store.fetch().getOrThrow()
    val initialCounts = InitialNotificationStoreCounts(store)
    storeRealTime.processMessage(RealTimeEventMock.SeenAllNotification)

    // THEN
    contentObserver.reloadStoreCounter.shouldBeExactly(0)
    contentObserver.didChangeCounter.shouldBeExactly(0)
    contentObserver.didDeleteCounter.shouldBeExactly(1)
    contentObserver.didDeleteSpy[0].indexes.shouldBe(0.until(initialCounts.totalCount).toList())
    store.totalCount.shouldBeExactly(0)
    store.unreadCount.shouldBeExactly(0)
    store.unseenCount.shouldBeExactly(0)
    Unit
  }

  @Test
  fun test_notifyNewNotification_WithDefaultStorePredicate_ShouldRefreshStoreAndCounters() = runBlocking {
    // GIVEN
    val countObserver = CountObserverMock()
    val predicate = StorePredicate()
    val storePage = givenPageStore(predicate, defaultEdgeArraySize)
    val store = createStoreDirector(
      predicate,
      Result.success(storePage)
    )
    store.addCountObserver(countObserver)

    // WHEN
    store.fetch().getOrThrow()
    storeRealTime.processMessage(RealTimeEventMock.NewNotification("NewId"))

    // THEN
    delay(100)
    countObserver.totalCountCounter.shouldBeExactly(2)
    countObserver.unreadCountCounter.shouldBeExactly(2)
    countObserver.unseenCountCounter.shouldBeExactly(2)
    Unit
  }

  @Test
  fun test_notifyReadNotification_WithDefaultStorePredicateAndUnread_ShouldRefreshStoreAndCounters() = runBlocking {
    // GIVEN
    val countObserver = CountObserverMock()
    val predicate = StorePredicate()
    val storePage = givenPageStore(predicate, defaultEdgeArraySize, forceProperty = ForceProperty.Unread)
    val store = createStoreDirector(
      predicate,
      Result.success(storePage)
    )
    store.addCountObserver(countObserver)

    // WHEN
    store.fetch().getOrThrow()
    val initialCounts = InitialNotificationStoreCounts(store)
    val chosenIndex = anyIndexForDefaultEdgeArraySize
    storeRealTime.processMessage(RealTimeEventMock.ReadNotification(chosenIndex.toString()))


    // THEN
    countObserver.totalCountCounter.shouldBeExactly(1)
    countObserver.totalCountSpy[0].count.shouldBeExactly(store.totalCount)
    countObserver.unreadCountCounter.shouldBeExactly(2)
    countObserver.unreadCountSpy[0].count.shouldBeExactly(initialCounts.unreadCount)
    countObserver.unreadCountSpy[1].count.shouldBeExactly(initialCounts.unreadCount - 1)
    Unit
  }

  @Test
  fun test_notifyReadNotification_WithDefaultStorePredicateAndRead_ShouldRefreshStoreAndCounters() = runBlocking {
    // GIVEN
    val countObserver = CountObserverMock()
    val predicate = StorePredicate()
    val storePage = givenPageStore(predicate, defaultEdgeArraySize, forceProperty = ForceProperty.Read)
    val store = createStoreDirector(
      predicate,
      Result.success(storePage)
    )
    store.addCountObserver(countObserver)

    // WHEN
    store.fetch().getOrThrow()
    val chosenIndex = anyIndexForDefaultEdgeArraySize
    storeRealTime.processMessage(RealTimeEventMock.ReadNotification(chosenIndex.toString()))

    // THEN
    delay(100)
    countObserver.totalCountCounter.shouldBeExactly(1)
    countObserver.totalCountSpy[0].count.shouldBeExactly(store.size)
    countObserver.unreadCountCounter.shouldBeExactly(0)
    Unit
  }

  @Test
  fun test_notifyReadNotification_WithDefaultStorePredicateAndUnseen_ShouldRefreshStoreAndCounters() = runBlocking {
    // GIVEN
    val countObserver = CountObserverMock()
    val predicate = StorePredicate()
    val storePage = givenPageStore(predicate, defaultEdgeArraySize, forceProperty = ForceProperty.Unseen)
    val store = createStoreDirector(
      predicate,
      Result.success(storePage)
    )
    store.addCountObserver(countObserver)

    // WHEN
    store.fetch().getOrThrow()
    val initialCounts = InitialNotificationStoreCounts(store)
    val chosenIndex = anyIndexForDefaultEdgeArraySize
    storeRealTime.processMessage(RealTimeEventMock.ReadNotification(chosenIndex.toString()))

    // THEN
    countObserver.totalCountCounter.shouldBeExactly(1)
    countObserver.totalCountSpy[0].count.shouldBeExactly(store.size)
    countObserver.unseenCountCounter.shouldBeExactly(2)
    countObserver.unseenCountSpy[0].count.shouldBeExactly(initialCounts.unseenCount)
    countObserver.unseenCountSpy[1].count.shouldBeExactly(initialCounts.unseenCount - 1)
    Unit
  }

  @Test
  fun test_notifyReadNotification_WithDefaultStorePredicateAndSeen_ShouldRefreshStoreAndCounters() = runBlocking {
    // GIVEN
    val countObserver = CountObserverMock()
    val predicate = StorePredicate()
    val storePage = givenPageStore(predicate, defaultEdgeArraySize, forceProperty = ForceProperty.Seen)
    val store = createStoreDirector(
      predicate,
      Result.success(storePage)
    )
    store.addCountObserver(countObserver)

    // WHEN
    store.fetch().getOrThrow()
    val chosenIndex = anyIndexForDefaultEdgeArraySize
    storeRealTime.processMessage(RealTimeEventMock.ReadNotification(chosenIndex.toString()))

    // THEN
    countObserver.totalCountCounter.shouldBeExactly(1)
    countObserver.totalCountSpy[0].count.shouldBeExactly(store.size)
    countObserver.unseenCountCounter.shouldBeExactly(0)
    Unit
  }

  @Test
  fun test_notifyReadNotification_WithUnreadStorePredicateAndUnread_ShouldRefreshStoreAndCounters() = runBlocking {
    // GIVEN
    val countObserver = CountObserverMock()
    val predicate = StorePredicate(read = false)
    val storePage = givenPageStore(predicate, defaultEdgeArraySize, forceProperty = ForceProperty.Unread)
    val store = createStoreDirector(
      predicate,
      Result.success(storePage)
    )
    store.addCountObserver(countObserver)

    // WHEN
    store.fetch().getOrThrow()
    val initialCounts = InitialNotificationStoreCounts(store)
    val chosenIndex = anyIndexForDefaultEdgeArraySize
    storeRealTime.processMessage(RealTimeEventMock.ReadNotification(chosenIndex.toString()))

    // THEN
    delay(100)
    countObserver.totalCountCounter.shouldBeExactly(2)
    countObserver.unreadCountCounter.shouldBeExactly(2)
    countObserver.unreadCountSpy[0].count.shouldBeExactly(initialCounts.unreadCount)
    countObserver.unreadCountSpy[1].count.shouldBeExactly(initialCounts.unreadCount - 1)
    Unit
  }

  @Test
  fun test_notifyReadNotification_WithUnreadStorePredicateAndRead_ShouldRefreshStoreAndCounters() = runBlocking {
    // GIVEN
    val countObserver = CountObserverMock()
    val predicate = StorePredicate(read = false)
    val storePage = givenPageStore(predicate, defaultEdgeArraySize, forceProperty = ForceProperty.Read)
    val store = createStoreDirector(
      predicate,
      Result.success(storePage)
    )
    store.addCountObserver(countObserver)

    // WHEN
    store.fetch().getOrThrow()
    val chosenIndex = anyIndexForDefaultEdgeArraySize
    storeRealTime.processMessage(RealTimeEventMock.ReadNotification(chosenIndex.toString()))

    countObserver.totalCountCounter.shouldBeExactly(1)
    countObserver.unreadCountCounter.shouldBeExactly(0)
    Unit
  }

  @Test
  fun test_notifyReadNotification_WithUnreadStorePredicateAndUnseen_ShouldRefreshStoreAndCounters() = runBlocking {
    // GIVEN
    val countObserver = CountObserverMock()
    val predicate = StorePredicate(read = false)
    val storePage = givenPageStore(predicate, defaultEdgeArraySize, forceProperty = ForceProperty.Unseen)
    val store = createStoreDirector(
      predicate,
      Result.success(storePage)
    )
    store.addCountObserver(countObserver)

    // WHEN
    store.fetch().getOrThrow()
    val initialCounts = InitialNotificationStoreCounts(store)
    val chosenIndex = anyIndexForDefaultEdgeArraySize
    storeRealTime.processMessage(RealTimeEventMock.ReadNotification(chosenIndex.toString()))

    // THEN
    countObserver.totalCountCounter.shouldBeExactly(2)
    countObserver.totalCountSpy[0].count.shouldBeExactly(initialCounts.totalCount)
    countObserver.totalCountSpy[1].count.shouldBeExactly(initialCounts.totalCount - 1)
    countObserver.unseenCountCounter.shouldBeExactly(2)
    countObserver.unseenCountSpy[0].count.shouldBeExactly(initialCounts.unseenCount)
    countObserver.unseenCountSpy[1].count.shouldBeExactly(initialCounts.unseenCount - 1)
    Unit
  }

  @Test
  fun test_notifyReadNotification_WithUnreadStorePredicateAndSeen_ShouldRefreshStoreAndCounters() = runBlocking {
    // GIVEN
    val countObserver = CountObserverMock()
    val predicate = StorePredicate(read = false)
    val storePage = givenPageStore(predicate, defaultEdgeArraySize, forceProperty = ForceProperty.Seen)
    val store = createStoreDirector(
      predicate,
      Result.success(storePage)
    )
    store.addCountObserver(countObserver)

    // WHEN
    store.fetch().getOrThrow()
    val initialCounts = InitialNotificationStoreCounts(store)
    val chosenIndex = anyIndexForDefaultEdgeArraySize
    storeRealTime.processMessage(RealTimeEventMock.ReadNotification(chosenIndex.toString()))

    // THEN
    countObserver.totalCountCounter.shouldBeExactly(2)
    countObserver.totalCountSpy[0].count.shouldBeExactly(initialCounts.totalCount)
    countObserver.totalCountSpy[1].count.shouldBeExactly(initialCounts.totalCount - 1)
    countObserver.unseenCountCounter.shouldBeExactly(0)
    Unit
  }

  @Test
  fun test_notifyReadAllNotification_WithDefaultStorePredicateAndRead_ShouldNotifyCounters() = runBlocking {
    // GIVEN
    val countObserver = CountObserverMock()
    val predicate = StorePredicate()
    val storePage = givenPageStore(predicate, defaultEdgeArraySize)
    val store = createStoreDirector(
      predicate,
      Result.success(storePage)
    )
    store.addCountObserver(countObserver)

    // WHEN
    store.fetch().getOrThrow()
    val initialCounts = InitialNotificationStoreCounts(store)
    coEvery { fetchStorePageInteractor.invoke(any(), any(), any()) } returns givenPageStore(predicate, 15, forceProperty = ForceProperty.Read)
    storeRealTime.processMessage(RealTimeEventMock.ReadAllNotification)

    // THEN
    delay(100)
    countObserver.totalCountCounter.shouldBeExactly(2)
    countObserver.unreadCountCounter.shouldBeExactly(1)
    countObserver.unreadCountSpy[0].count.shouldBeExactly(initialCounts.unreadCount)
    countObserver.unseenCountCounter.shouldBeExactly(1)
    Unit
  }

  @Test
  fun test_notifySeenAllNotification_WithDefaultStorePredicate_ShouldNotifyCounters() = runBlocking {
    // GIVEN
    val countObserver = CountObserverMock()
    val predicate = StorePredicate()
    val storePage = givenPageStore(predicate, defaultEdgeArraySize, forceProperty = ForceProperty.Unread)
    val store = createStoreDirector(
      predicate,
      Result.success(storePage)
    )
    store.addCountObserver(countObserver)

    // WHEN
    store.fetch().getOrThrow()
    val initialCounts = InitialNotificationStoreCounts(store)
    coEvery { fetchStorePageInteractor.invoke(any(), any(), any()) } returns givenPageStore(predicate, 15, forceProperty = ForceProperty.Read)
    storeRealTime.processMessage(RealTimeEventMock.SeenAllNotification)

    // THEN
    delay(100)
    countObserver.totalCountCounter.shouldBeExactly(2)
    countObserver.unreadCountCounter.shouldBeExactly(1)
    countObserver.unreadCountSpy[0].count.shouldBeExactly(initialCounts.unreadCount)
    countObserver.unseenCountCounter.shouldBeExactly(1)
    countObserver.unseenCountSpy[0].count.shouldBeExactly(initialCounts.unseenCount)
    Unit
  }

  @Test
  fun test_removeCountObserver() = runBlocking {
    // GIVEN
    val countObserver = CountObserverMock()
    val predicate = StorePredicate()
    val storePage = givenPageStore(predicate, defaultEdgeArraySize, forceProperty = ForceProperty.Unread)
    val store = createStoreDirector(
      predicate,
      Result.success(storePage)
    )

    // WHEN
    store.addCountObserver(countObserver)
    store.removeCountObserver(countObserver)
    storeRealTime.processMessage(RealTimeEventMock.SeenAllNotification)
    store.fetch().getOrThrow()

    // THEN
    countObserver.totalCountCounter.shouldBeExactly(0)
    Unit
  }

  @Test
  fun test_removeContentObserver() = runBlocking {
    // GIVEN
    val contentObserver = ContentObserverMock()
    val predicate = StorePredicate()
    val storePage = givenPageStore(predicate, defaultEdgeArraySize, forceProperty = ForceProperty.Unread)
    val store = createStoreDirector(
      predicate,
      Result.success(storePage)
    )

    // WHEN
    store.addContentObserver(contentObserver)
    store.removeContentObserver(contentObserver)
    storeRealTime.processMessage(RealTimeEventMock.SeenAllNotification)
    store.fetch().getOrThrow()

    // THEN
    contentObserver.didInsertCounter.shouldBeExactly(0)
    Unit
  }

  @Test
  fun test_notifyArchiveNotification_withDefaultStorePredicateAndExists_shouldDoNothing() = runBlocking {
    // GIVEN
    val predicate = StorePredicate()
    val storePage = givenPageStore(predicate, defaultEdgeArraySize, forceProperty = ForceProperty.Unarchived)
    val store = createStoreDirector(
      predicate,
      Result.success(storePage)
    )

    // WHEN
    store.fetch().getOrThrow()
    val initialCounter = InitialNotificationStoreCounts(store)
    val chosenIndex = anyIndexForDefaultEdgeArraySize
    storeRealTime.processMessage(
      RealTimeEventMock.ArchiveNotification(chosenIndex.toString())
    )

    // THEN
    coVerify(exactly = 1) { fetchStorePageInteractor.invoke(any(), any(), any()) }
    store.size.shouldBeExactly(defaultEdgeArraySize - 1)
    store.totalCount.shouldBeExactly(initialCounter.totalCount - 1)
    Unit
  }
}