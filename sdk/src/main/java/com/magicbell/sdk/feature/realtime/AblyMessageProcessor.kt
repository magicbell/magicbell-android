package com.magicbell.sdk.feature.realtime

import com.google.gson.JsonObject
import com.magicbell.sdk.common.error.MagicBellError
import io.ably.lib.types.Message

internal class AblyMessageProcessor {

  sealed class RealTimeMessage {
    class New(val notificationId: String) : RealTimeMessage()
    class Read(val notificationId: String) : RealTimeMessage()
    class Unread(val notificationId: String) : RealTimeMessage()
    class Delete(val notificationId: String) : RealTimeMessage()
    class Archived(val notificationId: String) : RealTimeMessage()
    object ReadAll : RealTimeMessage()
    object SeenAll : RealTimeMessage()
  }

  fun processMessage(message: Message): RealTimeMessage {
    val event = message.name
    val eventData = message.data as JsonObject
    val eventParts = event.split("/", limit = 2)

    if (eventParts.isNotEmpty() && eventParts.size == 2 && eventParts[0] == "notifications") {
      val notificationId = if (eventData.has("id")) eventData.getAsJsonPrimitive("id").asString else null
      return obtainMessage(eventParts[1], notificationId)
    } else {
      throw MagicBellError("Ably event cannot be handled")
    }
  }

  private fun obtainMessage(eventName: String, notificationId: String?): RealTimeMessage {
    if (notificationId != null) {
      return when (eventName) {
        "new" -> RealTimeMessage.New(notificationId)
        "read" -> RealTimeMessage.Read(notificationId)
        "unread" -> RealTimeMessage.Unread(notificationId)
        "delete" -> RealTimeMessage.Delete(notificationId)
        "archived" -> RealTimeMessage.Archived(notificationId)
        else -> throw MagicBellError("Ably event cannot be handled")
      }
    } else {
      return when (eventName) {
        "read/all" -> RealTimeMessage.ReadAll
        "seen/all" -> RealTimeMessage.SeenAll
        else -> throw MagicBellError("Ably event cannot be handled")
      }
    }
  }
}