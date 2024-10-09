package com.magicbell.sdk.feature.store

import com.magicbell.sdk.feature.notification.Notification
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@Serializable
internal class StorePage(
  val notifications: List<Notification>,
  @JsonNames("total")
  val totalCount: Int,
  @JsonNames("unread_count")
  val unreadCount: Int,
  @JsonNames("unseen_count")
  val unseenCount: Int,
  @JsonNames("total_pages")
  val totalPages: Int,
  @JsonNames("per_page")
  val perPage: Int,
  @JsonNames("current_page")
  val currentPage: Int
)
