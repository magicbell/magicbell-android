package com.magicbell.sdk.common.logger

import com.harmony.kotlin.common.logger.AndroidLogger
import com.harmony.kotlin.common.logger.Logger

enum class LogLevel {

  NONE {
    override fun logger(): Logger {
      return VoidLogger()
    }
  },
  DEBUG {
    override fun logger(): Logger {
      return AndroidLogger(true)
    }
  };

  internal abstract fun logger(): Logger
}