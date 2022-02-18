package com.magicbell.sdk.common.threading

import android.os.Handler

interface MainThread {
  fun post(run: () -> Unit)
}

class MainThreadExecutor(private val handler: Handler) : MainThread {
  override fun post(run: () -> Unit) {
    handler.post(run)
  }
}
