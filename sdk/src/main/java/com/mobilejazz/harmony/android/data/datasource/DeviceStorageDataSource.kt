package com.mobilejazz.harmony.android.data.datasource

import android.content.SharedPreferences
import com.mobilejazz.harmony.data.datasource.DeleteDataSource
import com.mobilejazz.harmony.data.datasource.GetDataSource
import com.mobilejazz.harmony.data.datasource.PutDataSource
import com.mobilejazz.harmony.data.error.DataNotFoundException
import com.mobilejazz.harmony.data.query.AllObjectsQuery
import com.mobilejazz.harmony.data.query.KeyQuery
import com.mobilejazz.harmony.data.query.Query

class DeviceStorageDataSource<T>(
  private val sharedPreferences: SharedPreferences,
  private val prefix: String = ""
) : GetDataSource<T>, PutDataSource<T>, DeleteDataSource {

  override suspend fun get(query: Query): T =
    when (query) {
      is KeyQuery -> {
        val key = addPrefixTo(query.key)
        if (!sharedPreferences.contains(key)) {
          throw DataNotFoundException()
        }

        @Suppress("UNCHECKED_CAST")
        sharedPreferences.all[key] as T
      }
      else -> notSupportedQuery()
    }

  override suspend fun getAll(query: Query): List<T> = throw UnsupportedOperationException("getAll not supported. Use get instead")

  override suspend fun put(query: Query, value: T?): T =
    when (query) {
      is KeyQuery -> {
        value?.let {
          val key = addPrefixTo(query.key)
          val editor = sharedPreferences.edit()
          when (value) {
            is String -> editor.putString(key, value).apply()
            is Boolean -> editor.putBoolean(key, value).apply()
            is Float -> editor.putFloat(key, value).apply()
            is Int -> editor.putInt(key, value).apply()
            is Long -> editor.putLong(key, value).apply()
            is Set<*> -> {
              @Suppress("UNCHECKED_CAST")
              (value as? Set<String>)?.let { castedValue ->
                editor.putStringSet(key, castedValue).apply()
              } ?: throw UnsupportedOperationException("value type is not supported")
            }
            else -> {
              throw UnsupportedOperationException("value type is not supported")
            }
          }

          return@let it
        } ?: throw IllegalArgumentException("${DeviceStorageDataSource::class.java.simpleName}: value must be not null")
      }
      else -> notSupportedQuery()
    }

  override suspend fun putAll(query: Query, value: List<T>?): List<T> = throw UnsupportedOperationException("putAll not supported. Use put instead")

  override suspend fun delete(query: Query) =
    when (query) {
      is AllObjectsQuery -> {
        with(sharedPreferences.edit()) {
          if (prefix.isNotEmpty()) {
            sharedPreferences.all.keys.filter { it.contains(prefix) }.forEach { remove(it) }
          } else {
            clear()
          }
          apply()
        }
      }
      is KeyQuery -> {
        sharedPreferences.edit()
          .remove(addPrefixTo(query.key))
          .apply()
      }
      else -> notSupportedQuery()
    }

  private fun addPrefixTo(key: String) = if (prefix.isEmpty()) key else "$prefix.$key"
}
