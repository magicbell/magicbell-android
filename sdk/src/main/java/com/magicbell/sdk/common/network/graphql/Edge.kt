package com.magicbell.sdk.common.network.graphql

import kotlinx.serialization.Serializable

@Serializable
internal class Edge<T>(val cursor: String, val node: T)