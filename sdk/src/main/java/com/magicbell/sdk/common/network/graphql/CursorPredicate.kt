package com.magicbell.sdk.common.network.graphql

internal class CursorPredicate(val cursor: Cursor = Cursor.Unspecified, val size: Int? = null) : GraphQLRepresentable {
  sealed class Cursor {
    class Next(val value: String) : Cursor()
    class Previous(val value: String) : Cursor()
    object Unspecified : Cursor()
  }

  override val graphQLValue: String
    get() {
      val cursorParams = mutableListOf<String>()

      when (cursor) {
        is Cursor.Next -> {
          cursorParams.add(cursor.value)
        }
        is Cursor.Previous -> {
          cursorParams.add(cursor.value)
        }
        Cursor.Unspecified -> {
          // Do nothing
        }
      }

      size?.also {
        cursorParams.add("first: $it")
      }

      return cursorParams.joinToString(",, ")
    }
}