package com.magicbell.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.magicbell.sdk.User
import com.magicbell.sdk.feature.notification.Notification
import com.magicbell.sdk.feature.store.NotificationStore
import com.magicbell.sdk.feature.store.archive
import com.magicbell.sdk.feature.store.delete
import com.magicbell.sdk.feature.store.forAll
import com.magicbell.sdk.feature.store.markAllNotificationAsRead
import com.magicbell.sdk.feature.store.markAllNotificationAsSeen
import com.magicbell.sdk.feature.store.markAsRead
import com.magicbell.sdk.feature.store.markAsUnread
import com.magicbell.sdk.feature.store.unarchive
import com.magicbell.sdk_compose.NotificationStoreViewModel

class MainActivityCompose : ComponentActivity() {

  private lateinit var user: User
  private lateinit var store: NotificationStore

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    initMagicBell()
  }

  private fun initMagicBell() {
    user = (application as ExampleApplication).magicBellClient.connectUserEmail("javier@mobilejazz.com")
    store = user.store.forAll()
    setContent {
      NotificationStoreScreen(notificationStoreViewModel = NotificationStoreViewModel(store))
    }
  }

  @Composable
  private fun NotificationStoreScreen(@Suppress("UNUSED_PARAMETER") notificationStoreViewModel: NotificationStoreViewModel) {
  }

  @Composable
  private fun BottomSheetView(notification: Notification) {
    Column {
      if (notification.isRead) {
        TextButton(onClick = {
          store.markAsUnread(notification, onSuccess = {}, onFailure = {})
        }) {
          Text(text = "Mark Unread")
        }
      } else {
        TextButton(onClick = {
          store.markAsRead(notification, onSuccess = {}, onFailure = {})
        }) {
          Text(text = "Mark Read")
        }
      }

      if (notification.isRead) {
        TextButton(onClick = {
          store.archive(notification, onSuccess = {}, onFailure = {})
        }) {
          Text(text = "Archive")
        }
      } else {
        TextButton(onClick = {
          store.unarchive(notification, onSuccess = {}, onFailure = {})
        }) {
          Text(text = "Unarchive")
        }
      }

      TextButton(onClick = {
        store.delete(notification, onCompletion = {}, onFailure = {})
      }) {
        Text(text = "Delete")
      }
    }
  }

  @Composable
  fun TopAppBarDropdownMenu() {
    val expanded = remember { mutableStateOf(false) }
    Box(
      Modifier
        .wrapContentSize(Alignment.TopEnd)
    ) {
      IconButton(onClick = {
        expanded.value = true
      }) {
        Icon(
          Icons.Filled.MoreVert,
          contentDescription = "More Menu"
        )
      }
    }

    DropdownMenu(
      expanded = expanded.value,
      onDismissRequest = { expanded.value = false },
    ) {
      DropdownMenuItem(onClick = {
        expanded.value = false
        store.markAllNotificationAsRead(onSuccess = {}, onFailure = {})
      }) {}

      Divider()

      DropdownMenuItem(onClick = {
        expanded.value = false
        store.markAllNotificationAsSeen(onSuccess = {}, onFailure = {})
      }) {}
    }
  }

  @Composable
  private fun NotificationStoreView(notificationStoreViewModel: NotificationStoreViewModel) {
    val listState = rememberLazyListState()
    LazyColumn(state = listState) {
      items(items = notificationStoreViewModel.notifications, itemContent = { item ->
        NotificationRow(notification = item)
        Divider()
      })
    }
  }

  @Composable
  private fun NotificationRow(notification: Notification) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(20.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(Modifier.weight(0.9f)) {
        Text(
          text = notification.title,
          style = MaterialTheme.typography.h6,
          modifier = Modifier
            .wrapContentHeight()
        )

        if (!notification.content.isNullOrBlank()) {
          Text(
            text = notification.content!!,
            style = MaterialTheme.typography.body2,
            modifier = Modifier
              .padding(top = 4.dp)
              .wrapContentHeight()
          )
        }
      }
      Row(
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
          .weight(0.1f)
      ) {
        if (!notification.isRead) {
          Box(
            modifier = Modifier
              .size(6.dp)
              .clip(CircleShape)
              .background(colorResource(id = R.color.primaryColor))
          )
        }
      }
    }
  }
}