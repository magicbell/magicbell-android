package com.magicbell.sdk.common.environment

import java.net.URL

internal class Environment(val apiKey: String, val apiSecret: String?, val baseUrl: URL, val isHMACEnabled: Boolean)
