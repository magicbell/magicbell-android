package com.magicbell.example

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.messaging.FirebaseMessaging
import com.magicbell.example.adapter.EndlessRecyclerViewScrollListener
import com.magicbell.example.adapter.NotificationsAdapter
import com.magicbell.example.databinding.ActivityMainBinding
import com.magicbell.example.modal.NotificationActionsSheetFragment
import com.magicbell.sdk.User
import com.magicbell.sdk.feature.notification.Notification
import com.magicbell.sdk.feature.store.NotificationStore
import com.magicbell.sdk.feature.store.NotificationStoreContentObserver
import com.magicbell.sdk.feature.store.NotificationStoreCountObserver
import com.magicbell.sdk.feature.store.StorePredicate
import com.magicbell.sdk.feature.store.archive
import com.magicbell.sdk.feature.store.delete
import com.magicbell.sdk.feature.store.fetch
import com.magicbell.sdk.feature.store.forAll
import com.magicbell.sdk.feature.store.markAllNotificationAsRead
import com.magicbell.sdk.feature.store.markAllNotificationAsSeen
import com.magicbell.sdk.feature.store.markAsRead
import com.magicbell.sdk.feature.store.markAsUnread
import com.magicbell.sdk.feature.store.refresh
import com.magicbell.sdk.feature.store.unarchive
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MainActivity : AppCompatActivity(), NotificationActionsSheetFragment.ActionListener, NotificationStoreContentObserver, NotificationStoreCountObserver {

  private lateinit var binding: ActivityMainBinding

  private lateinit var user: User
  private lateinit var store: NotificationStore

  private val notificationsAdapter by lazy {
    @Suppress("UNUSED_ANONYMOUS_PARAMETER")
    NotificationsAdapter(store = store) { notification, position ->
      NotificationActionsSheetFragment.newInstance(notification, this).show(supportFragmentManager, NotificationActionsSheetFragment::class.java.canonicalName)
    }
  }

  private lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>
  private lateinit var scrollListener: EndlessRecyclerViewScrollListener

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    initMagicBell()

    binding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(binding.root)
    setSupportActionBar(binding.toolbar)

    bottomSheetBehavior = BottomSheetBehavior.from(findViewById(R.id.notification_action_sheet))
    bottomSheetBehavior.peekHeight = 0
    setBottomSheetVisibility(false)

    configRecyclerView()

    reloadStore()

    // Only needed on API 33+
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      askNotificationPermission()
    }
  }

  private fun configRecyclerView() {
    val recyclerView = binding.notificationRv
    val linearLayoutManager = LinearLayoutManager(this)

    val dividerItemDecoration = DividerItemDecoration(
      recyclerView.context,
      linearLayoutManager.orientation
    )
    recyclerView.addItemDecoration(dividerItemDecoration)

    recyclerView.layoutManager = linearLayoutManager
    scrollListener = object : EndlessRecyclerViewScrollListener(linearLayoutManager) {
      override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
        if (!store.hasNextPage) {
          return
        }

        store.fetch(onSuccess = {

        }, onFailure = {

        })
      }
    }
    recyclerView.adapter = notificationsAdapter
    recyclerView.addOnScrollListener(scrollListener)
  }

  private fun initMagicBell() {
    user = (application as ExampleApplication).magicBellClient.connectUserEmail("john@doe.com")
    store = user.store.forAll()
    store.addContentObserver(this)
    store.addCountObserver(this)
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    val menuInflater = menuInflater
    menuInflater.inflate(R.menu.notification_menu, menu)
    return true
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    when (item.itemId) {
      R.id.change_user -> {
        val taskEditText = AppCompatEditText(this)

        @Suppress("UNUSED_ANONYMOUS_PARAMETER")
        val dialog: AlertDialog = AlertDialog.Builder(this)
          .setTitle("Change user")
          .setMessage("Insert user's email")
          .setView(taskEditText)
          .setPositiveButton("Login") { dialog, which ->
            val email = taskEditText.text.toString()
            user = (application as ExampleApplication).magicBellClient.connectUserEmail(email)
            configureStore(StorePredicate())
          }
          .setNegativeButton("Cancel", null)
          .create()
        dialog.show()
      }
      R.id.customize_predicate -> {
        val colors = arrayOf("All", "Read", "Unread", "Archived")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Change predicate")

        @Suppress("UNUSED_ANONYMOUS_PARAMETER")
        builder.setItems(colors) { dialog, which ->
          when (which) {
            0 -> configureStore(StorePredicate())
            1 -> configureStore(StorePredicate(read = true))
            2 -> configureStore(StorePredicate(read = false))
            3 -> configureStore(StorePredicate(archived = true))
            else -> {}
          }
        }
        builder.show()
      }
      R.id.mark_all_read -> {
        store.markAllNotificationAsRead(onSuccess = {}, onFailure = {})
      }

      R.id.mark_all_seen -> {
        store.markAllNotificationAsSeen(onSuccess = {}, onFailure = {})
      }
      else -> {}
    }

    return true
  }

  private fun configureStore(predicate: StorePredicate) {
    store.removeContentObserver(this)
    store.removeCountObserver(this)

    store = user.store.build(predicate)

    store.addContentObserver(this)
    store.addCountObserver(this)

    reloadStore()
  }

  @SuppressLint("NotifyDataSetChanged")
  private fun reloadStore() {
    if (store.count == 0) {
      store.refresh(onSuccess = {
        title = "Notifications - ${store.totalCount}"
        scrollListener.resetState()
        notificationsAdapter.store = store
        notificationsAdapter.notifyDataSetChanged()
      }, onFailure = {
        println(it)
      })
    } else {
      notificationsAdapter.notifyDataSetChanged()
      // TODO: 20/1/22 add badge
    }
  }

  private fun setBottomSheetVisibility(isVisible: Boolean) {
    val updatedState = if (isVisible) BottomSheetBehavior.STATE_EXPANDED else BottomSheetBehavior.STATE_COLLAPSED
    bottomSheetBehavior.state = updatedState
  }

  override fun onArchiveClick(notification: Notification, isArchived: Boolean) {
    if (isArchived) {
      store.unarchive(notification, onSuccess = {}, onFailure = {})
    } else {
      store.archive(notification, onSuccess = {}, onFailure = {})
    }
  }

  override fun onReadClick(notification: Notification, isRead: Boolean) {
    if (isRead) {
      store.markAsUnread(notification, onSuccess = {}, onFailure = {})
    } else {
      store.markAsRead(notification, onSuccess = {}, onFailure = {})
    }
  }

  override fun onDeleteClick(notification: Notification) {
    store.delete(notification, onCompletion = {}, onFailure = {})
  }

  override fun onStoreReloaded() {
    notificationsAdapter.notifyDataSetChanged()
  }

  override fun onNotificationsInserted(indexes: List<Int>) {
    notificationsAdapter.notifyItemRangeInserted(indexes.first(), indexes.last())
  }

  override fun onNotificationsChanged(indexes: List<Int>) {
    if (indexes.size == 1) {
      notificationsAdapter.notifyItemChanged(indexes.first())
    } else {
      notificationsAdapter.notifyItemRangeChanged(indexes.first(), indexes.last())
    }
  }

  override fun onNotificationsDeleted(indexes: List<Int>) {
    if (indexes.size == 1) {
      notificationsAdapter.notifyItemRemoved(indexes.first())
    } else {
      notificationsAdapter.notifyDataSetChanged()
    }
  }

  override fun onStoreHasNextPageChanged(hasNextPage: Boolean) {

  }

  override fun onTotalCountChanged(count: Int) {
    title = "Notifications - $count"
  }

  override fun onUnreadCountChanged(count: Int) {

  }

  override fun onUnseenCountChanged(count: Int) {

  }

  private fun updateDeviceToken() {
    val scope = CoroutineScope(Dispatchers.Main)
    scope.launch {
      val token = FirebaseMessaging.getInstance().token.await()
      (application as ExampleApplication).magicBellClient.setDeviceToken(token)
    }
  }
  @RequiresApi(Build.VERSION_CODES.TIRAMISU)
  private val requestPermissionLauncher = registerForActivityResult(
    ActivityResultContracts.RequestPermission(),
  ) { isGranted: Boolean ->
    if (isGranted) {
      // FCM SDK (and your app) can post notifications.
      updateDeviceToken()
    } else {
      // TODO: Inform user that that your app will not show notifications.
    }
  }
  @RequiresApi(Build.VERSION_CODES.TIRAMISU)
  private fun askNotificationPermission() {
    val permission = Manifest.permission.POST_NOTIFICATIONS

    when {
      ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED -> {
        // FCM SDK (and your app) can post notifications.
        updateDeviceToken()
      }
      shouldShowRequestPermissionRationale(permission) -> {
        // TODO: display an educational UI explaining to the user the features that will be enabled
        //       by them granting the POST_NOTIFICATION permission. This UI should provide the user
        //       "OK" and "No thanks" buttons. If the user selects "OK," directly request the permission.
        //       If the user selects "No thanks," allow the user to continue without notifications.
      }
      else -> {
        // Directly ask for the permission
        requestPermissionLauncher.launch(permission)
      }
    }
  }
}
