package com.magicbell.sdk.feature.store

import com.magicbell.sdk.common.network.graphql.Edge
import com.magicbell.sdk.common.network.graphql.PageInfo
import com.magicbell.sdk.feature.notification.Notification
import kotlinx.serialization.Serializable

@Serializable
internal class StorePage(
  val edges: List<Edge<Notification>>,
  val pageInfo: PageInfo,
  val totalCount: Int,
  val unreadCount: Int,
  val unseenCount: Int,
)
