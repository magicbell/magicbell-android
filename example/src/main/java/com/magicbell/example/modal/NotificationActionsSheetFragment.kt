package com.magicbell.example.modal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.magicbell.example.databinding.NotificationActionsSheetBinding
import com.magicbell.sdk.feature.notification.Notification

class NotificationActionsSheetFragment(
  private val notification: Notification,
  private val actionListener: ActionListener,
) : BottomSheetDialogFragment() {

  interface ActionListener {
    fun onArchiveClick(notification: Notification, isArchived: Boolean)
    fun onReadClick(notification: Notification, isRead: Boolean)
    fun onDeleteClick(notification: Notification)
  }

  companion object {
    fun newInstance(notification: Notification, actionListener: ActionListener) = NotificationActionsSheetFragment(notification, actionListener)
  }

  lateinit var binding: NotificationActionsSheetBinding

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    binding = NotificationActionsSheetBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    initDialog()
    val isRead = notification.readAt != null
    if (isRead) {
      binding.modalReadTv.text = "Mark Unread"
    } else {
      binding.modalReadTv.text = "Mark Read"
    }
    binding.modalReadTv.setOnClickListener {
      actionListener.onReadClick(notification, isRead)
      dismiss()
    }

    val isArchived = notification.archivedAt != null
    if (isArchived) {
      binding.modalArchiveTv.text = "Unarchive"
    } else {
      binding.modalArchiveTv.text = "Archive"
    }
    binding.modalArchiveTv.setOnClickListener {
      actionListener.onArchiveClick(notification, isArchived)
      dismiss()
    }

    binding.modalDeleteTv.setOnClickListener {
      actionListener.onDeleteClick(notification)
      dismiss()
    }
  }

  private fun initDialog() {
    requireDialog().window?.statusBarColor = requireContext().getColor(android.R.color.transparent)
  }
}