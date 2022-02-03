package com.magicbell.example.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.magicbell.example.R
import com.magicbell.sdk.feature.notification.Notification
import com.magicbell.sdk.feature.store.NotificationStore

class NotificationsAdapter(
  var store: NotificationStore,
  private val notificationClick: (Notification, Int) -> Unit,
) : RecyclerView.Adapter<NotificationsAdapter.ViewHolder>() {

  class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val titleView: TextView = view.findViewById(R.id.notification_title)
    val contentView: TextView = view.findViewById(R.id.notification_content)
    val readView: View = view.findViewById(R.id.notification_read_dot)
  }

  override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
    val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.notification_cell_view, viewGroup, false)
    return ViewHolder(view)
  }

  override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
    val notification = store[position]
    viewHolder.titleView.text = notification.title
    if (!notification.content.isNullOrBlank()) {
      viewHolder.contentView.visibility = View.VISIBLE
      viewHolder.contentView.text = notification.content
    } else {
      viewHolder.contentView.visibility = View.GONE
    }

    if (notification.isRead) {
      viewHolder.readView.visibility = View.INVISIBLE
    } else {
      viewHolder.readView.visibility = View.VISIBLE
    }

    viewHolder.itemView.setOnClickListener { notificationClick(notification, position) }
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
    super.onBindViewHolder(holder, position, payloads)
  }

  override fun getItemCount() = store.size
}
