package com.magicbell.example

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.magicbell.example.databinding.ActivityMainBinding
import com.magicbell.sdk.MagicBell
import com.magicbell.sdk.common.logger.LogLevel
import com.magicbell.sdk.common.query.UserQuery
import com.magicbell.sdk.feature.notification.data.NotificationActionQuery
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

  private lateinit var binding: ActivityMainBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    binding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(binding.root)

    setSupportActionBar(binding.toolbar)

    val magicBell = MagicBell("34ed17a8482e44c765d9e163015a8d586f0b3383", context = applicationContext, logLevel = LogLevel.DEBUG)
    GlobalScope.launch {
      val userQuery = UserQuery.createEmail(email = "javier@mobilejazz.com")
      println(magicBell.sdkComponent.getConfigInteractor().invoke(false, userQuery).channel)
      val notificationId = "94e9b0cd-ba82-4d69-9142-542385308ddc"
      magicBell.sdkComponent.getActionNotificationInteractor().invoke(NotificationActionQuery.Action.MARK_AS_READ, notificationId, userQuery)
      magicBell.sdkComponent.getActionNotificationInteractor().invoke(NotificationActionQuery.Action.MARK_AS_UNREAD, notificationId, userQuery)
      println(magicBell.sdkComponent.getNotificationInteractor().invoke(notificationId, userQuery))
    }
  }
}