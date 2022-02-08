# MagicBell Android SDK

This is the official [MagicBell](https://magicbell.com) SDK for Android.

This SDK offers:

- Real-time updates
- Low-level wrappers for the MagicBell API
- Support for the [Compose framework](https://developer.android.com/jetpack/compose)

It requires:

- API 23+
- Android Studio Arctic Fox

## Quick Start

First, grab your API key from your [MagicBell dashboard](https://app.magicbell.com). Then, initialize the client and set the current user:

```kotlin
import com.magicbell.sdk.MagicBellClient

// Create the MagicBell client with your project's API key
val magicbell = MagicBellClient(
  apiKey = "[MAGICBELL_API_KEY]",
  context = applicationContext
)

// Set the MagicBell user
val user = magicbell.connectUserEmail("richard@example.com")

// Create a store of notifications
val store = user.store.build()

// Fetch the first page of notifications. There is also a method without coroutine.
coroutineScope.launch {
  store.fetch().fold(
    onSuccess = { notificationList ->

    }, 
    onFailure = { error ->

    }
  )
}
```

This repo also contains a full blown example. To run the project:

- Clone the repo
- Open the root `build.gradle` in XCode
- Run `app` from the `Example` directory

## Table of Contents

- [Installation](#installation)
- [The MagicBell client](#the-magicbell-client)
- [User](#user)
    - [Multi-User Support](#multi-user-support)
    - [Logout a user](#logout-a-user)
    - [Integrating into your app](#integrating-into-your-app)
- [NotificationStore](#notificationstore)
    - [Obtaining a NotificationStore](#obtaining-a-notification-store)
    - [Observing NotificationStore changes](#observing-notification-store-changes)
    - [Notification Store adapter](#notification-store-adapter)
- [User Preferences](#user-preferences)
- [Push Notification Support](#push-notifications)
- [Contributing](#contributing)

## Installation

### Gradle

## The MagicBell Client

The first step is to create a `MagicBellClient` instance. It will manage users and other functionality for you. The API key for your MagicBell project is
required to initialize it.

```kotlin
val magicbell = MagicBellClient(
  apiKey = "[MAGICBELL_API_KEY]",
  context = applicationContext
)
```

You can provide additional options when initializing a client:

```kotlin
val magicbell = MagicBellClient(
  apiKey = "[MAGICBELL_API_KEY]",
  apiSecret = "[MAGICBELL_SECRET_KEY]",
  enableHMAC = false,
  baseURL = defaultBaseUrl,
  logLevel = LogLevel.NONE,
  context = applicationContext,
) 
```

| Param        | Default Value | Description                                                                                  |
| ------------ | ------------- | -------------------------------------------------------------------------------------------- |
| `apiKey`     | -             | Your MagicBell's API key                                                                     |
| `apiSecret`  | `nil`         | Your MagicBell's API secret                                                                  |
| `enableHMAC` | `false`       | Set it to `true` if you want HMAC enabled. Note the `apiSecret` is required if set to `true` |
| `logLevel`   | `.none`       | Set it to `.debug` to enable logs                                                            |

Though the API key is meant to be published, you should not distribute the API secret. Rather, enable HMAC in your project and generate the user secret on your
backend before distributing your app.

### Integrating into your app

You should create the client instance as early as possible in your application and ensure that only one instance is used across your application.

```kotlin
import com.magicbell.sdk.MagicBellClient

// Store the instance at a place of your convenience
val magicbell = MagicBellClient("[MAGICBELL_API_KEY]")
```

We recommend to create the instance in your Application class or in your Dependency Injection graph as a Singleton.

## User

Requests to the MagicBell API require that you **identify the MagicBell user**. This can be done by calling the
`connectUser(...)` method on the `MagicBellClient` instance with the user's email or external ID:

```kotlin
// Identify the user by its email
val user = magicbell.connectUserEmail("richard@example.com")

// Identify the user by its external id
val user = magicbell.connectUserExternalId("001")

// Identify the user by both, email and external id
val user = magicbell.connectUserWith(email = "richard@example.com", externalId = "0001")
```

You can connect as [many users as you need](#multi-user-support).

**IMPORTANT:** `User` instances are singletons. Therefore, calls to the `connectUser` method with the same arguments will yield the same user:

```kotlin
val userOne = magicbell.connectUserEmail("mary@example.com")
val userTwo = magicbell.connectUserEmail("mary@example.com")

assert(userOne === userTwo, "Both users reference to the same instance")
```

### Multi-User Support

If your app supports multiple logins, you may want to display the status of notifications for all logged in users at the same time. The MagicBell SDK allows you
to that.

You can call the `connectUser(:)` method with the r external ID of your logged in users as many times as you need.

```kotlin
val userOne = magicbell.connectUserEmail("richard@example.com")
val userTwo = magicbell.connectUserEmail("mary@example.com")
val userThree = magicbell.connectUserExternalId("001")
```

### Logout a User

When the user is logged out from your application you want to:

- Remove user's notifications from memory
- Stop the real-time connection with the MagicBell API
- Unregister the device from push notifications

This can be achieved with the `disconnectUser` method of the `MagicBell` client instance:

```kotlin
// Remove by email
magicbell.disconnectUserEmail("richard@example.com")

// Remove by external id
magicbell.disconnectUserExternalId("001")

// Remove by email and external id
magicbell.disconnectUserWith(email = "richard@example.com", externalId = "001")
```

### Integrating into your app

The MagicBell `User` instances need to available across your app. Here you have some options:

- extend your own user object
- define a global attribute
- use your own dependency injection graph

#### Extend your own user object

This approach is useful if you have a user object across your app. MagicBell will guarantee the `User` instance for a given email/externalId is unique, and you
only need to provide access to the instance. For example:

```kotlin

// Your own user
data class User {
  val name: String
  val email: String
}

/// Returns the logged in MagicBell user
fun User.magicBell(): MagicBell.User {
  return magicbell.connectUserEmail(email)
}
```

#### Define a global attribute

This is how you can define a nullable global variable that will represent your MagicBell user:

```kotlin
val magicbell = MagicBellClient("[MAGICBELL_API_KEY]")
var magicbellUser: MagicBell.User? = null
```

As soon as you perform a login, assign a value to this variable. Keep in mind, you will have to check the
`magicbellUser` variable was actually set before accessing it in your code.

#### Use your own dependency injection graph

You can also inject the MagicBell `User` instance in your own graph and keep track on it using your preferred pattern.

## NotificationStore

### Obtaining a notification store

The `NotificationStore` class represents a collection of [MagicBell](https://magicbell.com) notifications. You can create an instance of this class through
the `.build(...)` method on the user store object.

For example:

```kotlin
val allNotifications = user.store.build()

val readNotifications = user.store.build(read = true)

val unreadNotifications = user.store.build(read = false)

val archivedNotifications = user.store.build(archived = true)

val billingNotifications = user.store.build(categories = ["billing"])

val firstOrderNotifications = user.store.build(topics = ["order:001"])
```

These are the attributes of a notification store:

| Attributes    | Type             | Description                                                  |
| ------------- | ---------------- | ------------------------------------------------------------ |
| `totalCount`  | `Int`            | The total number of notifications                            |
| `unreadCount` | `Int`            | The number of unread notifications                           |
| `unseenCount` | `Int`            | The number of unseen notifications                           |
| `hasNextPage` | `Bool`           | Whether there are more items or not when paginating forwards |
| `count`       | `Int`            | The current number of notifications in the store             |
| `predicate`   | `StorePredicate` | The predicate used to filter notifications                   |

And these are the available methods:

| Method              | Description                                                  |
| ------------------- | ------------------------------------------------------------ |
| `refresh`           | Resets the store and fetches the first page of notifications |
| `fetch`             | Fetches the next page of notifications                       |
| `get(index:)`       | Subscript to access the notifications: `store[index]`        |
| `delete`            | Deletes a notification                                       |
| `delete`            | Deletes a notification                                       |
| `markAsRead`        | Marks a notification as read                                 |
| `markAsUnread`      | Marks a notification as unread                               |
| `archive`           | Archives a notification                                      |
| `unarchive`         | Unarchives a notification                                    |
| `markAllRead`       | Marks all notifications as read                              |
| `markAllUnseen`     | Marks all notifications as seen                              |

Most methods have two implementations:

- Using suspended functions returning a `Result` object
- Using lambdas returning `onSucess` or `onFailure`

```kotlin
// Delete notification. Lambdas
store.delete(
  notification,
  onCompletion = {
    println("Notification deleted")
  },
  onFailure = {
    print("Failed: ${error})")
  }
)

// Read a notification
store.markAsRead(notification).fold(
  onSuccess = {
    println("Notification marked as read")
  },
  onFailure = {
    println("Failed: $error")

  }
)
```

These methods ensure the state of the store is consistent when a notification changes. For example, when a notification is read, stores with the
predicate `read: .unread`, will remove that notification from themselves notifying all observers of the notification store.

### Advanced filters

You can also create stores with more advanced filters. To do it, fetch a store using the `.build(...)` method with a
`StorePredicate`.

```kotlin
val predicate = StorePredicate()
val notifications = user.store.build(predicate)
```

These are the available options:

| Param        | Options                            | Default        | Description                    |
| ------------ | ---------------------------------- | -------------- | ------------------------------ |
| `read`       | `true`, `false`, `nil` | `nil` | Filter by the `read` state (`nil` means unspecified)    |
| `seen`       | `true`, `false`, `nil` | `nil` | Filter by the `seen` state (`nil` means unspecified)    |
| `archived`   | `true`, `false`         | `false`  | Filter by the `archived` state |
| `categories` | `[String]`                         | `[]`           | Filter by categories          |
| `topics`     | `[String]`                         | `[]`           | Filter by topics               |

For example, use this predicate to fetch unread notifications of the `"important"` category:

```kotlin
val predicate = StorePredicate(read = true, categories = ["important"])
val store = user.store.build(predicate)
```

Notification stores are singletons. Creating a store with the same predicate twice will yield the same instance.

**Note**: Once a store is fetched, it will be kept alive in memory so it can be updated in real-time. You can force the removal of a store using the `.dispose`
method.

```kotlin
val predicate = StorePredicate()
user.store.dispose(predicate)
```

This is automatically done for you when you [remove a user instance](#logout-a-user).

### Observing changes

When either `fetch` or `refresh` is called, the store will notify the content observers with the newly added notifications (read about
observers [here](#observing-notification-store-changes)).

```kotlin
// Obtaining a new notification store (first time)
val store = user.store.build()

// First loading
val listNotifications = store.fetch().getOrElse {
  // An error occurred 
}
```

To reset and fetch the store:

```kotlin
    val listNotifications = store.refresh().getOrElse {
  // An error occurred 
}
```

### Accessing notifications

The `NotificationStore` is a list and has all list methods available. Therefore, notifications can be accessed as expected:

```kotlin
// forEach
store.forEach { notification ->
  println("Notification = $notification")
}

// for in
for (notification in store) {
  println("Notification = $notification")
}

// As an array
val notifications = store.notifications()
```

Enumeration is also available:

```kotlin
// forEach
store.forEachIndexed { index, notification ->
  println("Notification = $notification is in position $index")
}
```

### Observing notification store changes

#### Classic Observer Approach

Instances of `NotificationStore` are automatically updated when new notifications arrive, or a notification's state changes (marked read, archived, etc.)

To observe changes on a notification store, your observers must implement the following protocols:

```kotlin
// Get notified when the list of notifications of a notification store changes
interface NotificationStoreContentObserver {
  fun onStoreReloaded()
  fun onNotificationsChanged(indexes: List<Int>)
  fun onNotificationsDeleted(indexes: List<Int>)
  fun onStoreHasNextPageChanged(hasNextPage: Boolean)
}

// Get notified when the counters of a notification store change
interface NotificationStoreCountObserver {
  fun onTotalCountChanged(count: Int)
  fun onUnreadCountChanged(count: Int)
  fun onUnseenCountChanged(count: Int)
}
```

To observe changes, implement these protocols (or one of them), and register as an observer to a notification store.

```kotlin
val store = user.store.build()
val observer = myObserverClassInstance

store.addContentObserver(observer)
store.addCountObserver(observer)
```

#### Compose Approach

Use the class `NotificationStoreViewModel` to create a reactive object compatible with Compose and capable of publishing changes on the main attributes of a
`NotificaitonStore`.

This object must be created and retained by the user whenever it is needed.

| Attribute       | Type                        | Description                                        |
| --------------- | --------------------------- | -------------------------------------------------- |
| `totalCount`    | `State Int`            | The total count                                    |
| `unreadCount`   | `State Int`            | The unread count                                   |
| `unseenCount`   | `State Int`            | The unseen count                                   |
| `hasNextPage`   | `State Bool`           | Bool indicating if there is more content to fetch. |
| `notifications` | `State [Notification]` | The array of notifications.                        |

### Notification Store adapter

The `Notification Store` is a list also and we recommend to use it in your `RecyclerView` adapters. Thanks to the observers you can refresh your 
notification list very easy and with animations.
```kotlin
class NotificationsAdapter(
  var store: NotificationStore,
  private val notificationClick: (Notification, Int) -> Unit,
) : RecyclerView.Adapter<NotificationsAdapter.ViewHolder>()
```
Another option would be to have your own list of notifications and modify it everytime that the user does an action.


## User Preferences

You can fetch and set user preferences for MagicBell channels and categories.

```kotlin
class Preferences {
  var email: Bool
  var inApp: Bool
  var mobilePush: Bool
  var webPush: Bool
}

class UserPreferences {
  val preferences: Map<String, Preferences>
}
```

To fetch user preferences, use the `fetch` method as follows:

```kotlin
user.preferences.fetch().fold(onSuccess = { userPreferences ->
  println(preferences)
}, onFailure = {
  // An error occurred
})
```

It is also possible to fetch preference for a category using the `fetchPreferences(for:)` method:

```kotlin
user.preferences.fetchPreferences("important").fold(onSuccess = { preferences ->
  println(preferences)
}, onFailure = {
  // An error occurred
})
```

To update the preferences, use either `update` or `updatePreferences(:for:)`.

```kotlin
// Updating all preferences at once.
user.preferences.update().getOrElse { }

// Updating the list of preferences for a category
// Only preference for the included categories will be changed
user.preferences.update(categoryPreferences, "important").getOrElse { }
```

## Push Notifications

You can register the device token with MagicBell for mobile push notifications. To do it, set the device token as soon as it is provided by FCM or your
notification SDK:

```kotlin
// FCM Example
FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
  if (!task.isSuccessful) {
    Log.w(TAG, "Fetching FCM registration token failed", task.exception)
    return@OnCompleteListener
  }

  // Get new FCM registration token
  val token = task.result

  // Log and toast
  magicbell.setDeviceToken(token)
})
```

MagicBell will keep that device token stored temporarily in memory and send it as soon as new users are declared via
`MagicBellClient.connectUser`.

Whe a user is disconnected (`MagicBellClient.disconnectUser`), the device token is automatically unregistered for that user.

## Contributing

We welcome contributions of any kind. To do so, clone the repo and open `build.gradle` with Android Studio Arctic Fox or above.