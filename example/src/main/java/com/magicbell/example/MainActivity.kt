package com.magicbell.example

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.magicbell.example.databinding.ActivityMainBinding
import com.magicbell.sdk.MagicBellClient
import com.magicbell.sdk.common.logger.LogLevel
import com.magicbell.sdk.feature.store.StorePredicate
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

  private lateinit var binding: ActivityMainBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    binding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(binding.root)

    setSupportActionBar(binding.toolbar)

    val magicBell = MagicBellClient("34ed17a8482e44c765d9e163015a8d586f0b3383", context = applicationContext, logLevel = LogLevel.DEBUG)
    val user = magicBell.forUserEmail("javier@mobilejazz.com")
    GlobalScope.launch {
      user.store.with(StorePredicate()).refresh().fold(onSuccess = {
        println(it)
      }, onFailure = {
        println(it)
      })
    }

  }
}
