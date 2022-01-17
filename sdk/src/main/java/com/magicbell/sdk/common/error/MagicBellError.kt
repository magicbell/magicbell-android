package com.magicbell.sdk.common.error

import kotlinx.serialization.Serializable

@Serializable
open class MagicBellError(override val message: String? = "MagicBellError") : Exception(message)