package com.magicbell.sdk.common.network.graphql

import android.content.Context

internal class GraphQLFragment(filename: String, context: Context) : GraphQLRepresentable {
  override val graphQLValue: String = context.assets.open(filename).bufferedReader().use { it.readText().replace("\n", "") }
}