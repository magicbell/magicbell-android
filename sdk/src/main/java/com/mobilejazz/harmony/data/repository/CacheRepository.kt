package com.mobilejazz.harmony.data.repository

import com.mobilejazz.harmony.data.datasource.DeleteDataSource
import com.mobilejazz.harmony.data.datasource.GetDataSource
import com.mobilejazz.harmony.data.datasource.PutDataSource
import com.mobilejazz.harmony.data.error.DataNotFoundException
import com.mobilejazz.harmony.data.error.DataNotValidException
import com.mobilejazz.harmony.data.error.MappingException
import com.mobilejazz.harmony.data.error.OperationNotAllowedException
import com.mobilejazz.harmony.data.operation.CacheOperation
import com.mobilejazz.harmony.data.operation.CacheSyncOperation
import com.mobilejazz.harmony.data.operation.DefaultOperation
import com.mobilejazz.harmony.data.operation.MainOperation
import com.mobilejazz.harmony.data.operation.MainSyncOperation
import com.mobilejazz.harmony.data.operation.Operation
import com.mobilejazz.harmony.data.query.Query
import com.mobilejazz.harmony.data.repository.DeleteRepository
import com.mobilejazz.harmony.data.repository.GetRepository
import com.mobilejazz.harmony.data.repository.PutRepository
import com.mobilejazz.harmony.data.validator.Validator

class CacheRepository<V>(
  private val getCache: GetDataSource<V>,
  private val putCache: PutDataSource<V>,
  private val deleteCache: DeleteDataSource,
  private val getMain: GetDataSource<V>,
  private val putMain: PutDataSource<V>,
  private val deleteMain: DeleteDataSource,
  private val validator: Validator<V> = DefaultValidator()
) : GetRepository<V>, PutRepository<V>, DeleteRepository {

  override suspend fun get(query: Query, operation: Operation): V {
    return when (operation) {
      is DefaultOperation -> get(query, CacheSyncOperation())
      is MainOperation -> getMain.get(query)
      is CacheOperation -> {
        return try {
          val cacheValue = getCache.get(query)
          if (!validator.isValid(cacheValue)) {
            throw DataNotValidException()
          } else {
            cacheValue
          }
        } catch (cacheException: Exception) {
          if (operation.fallback(cacheException)) {
            getCache.get(query)
          } else {
            throw cacheException
          }
        }
      }
      is MainSyncOperation -> getMain.get(query).let {
        putCache.put(query, it)
      }
      is CacheSyncOperation -> {
        try {
          return getCache.get(query).let {
            if (!validator.isValid(it)) {
              throw DataNotValidException()
            } else {
              it
            }
          }
        } catch (cacheException: Exception) {
          try {
            when (cacheException) {
              is DataNotValidException,
              is MappingException,
              is DataNotFoundException -> get(query, MainSyncOperation)
              else -> throw cacheException
            }
          } catch (mainException: Exception) {
            if (operation.fallback(mainException, cacheException)) {
              getCache.get(query)
            } else {
              throw mainException
            }
          }
        }
      }
      else -> throw OperationNotAllowedException()
    }
  }

  override suspend fun getAll(query: Query, operation: Operation): List<V> {
    return when (operation) {
      is DefaultOperation -> getAll(query, CacheSyncOperation())
      is MainOperation -> getMain.getAll(query)
      is CacheOperation -> {
        return try {
          val cacheValues = getCache.getAll(query)
          val invalids = cacheValues.map { validator.isValid(it) }.filter { isValid -> !isValid }
          if (invalids.isNotEmpty()) {
            throw DataNotValidException()
          } else {
            cacheValues
          }
        } catch (cacheException: Exception) {
          if (operation.fallback(cacheException)) {
            getCache.getAll(query)
          } else {
            throw cacheException
          }
        }
      }

      is MainSyncOperation -> getMain.getAll(query).let { putCache.putAll(query, it) }
      is CacheSyncOperation -> {
        try {
          return getCache.getAll(query).map {
            if (!validator.isValid(it)) {
              throw DataNotValidException()
            } else {
              it
            }
          }
        } catch (cacheException: Exception) {
          try {
            when (cacheException) {
              is DataNotValidException,
              is MappingException,
              is DataNotFoundException -> getAll(query, MainSyncOperation)
              else -> throw cacheException
            }
          } catch (mainException: Exception) {
            if (operation.fallback(mainException, cacheException)) {
              getCache.getAll(query)
            } else {
              throw mainException
            }
          }
        }
      }
      else -> throw OperationNotAllowedException()
    }
  }

  override suspend fun put(query: Query, value: V?, operation: Operation): V = when (operation) {
    is DefaultOperation -> put(query, value, MainSyncOperation)
    is MainOperation -> putMain.put(query, value)
    is CacheOperation -> putCache.put(query, value)
    is MainSyncOperation -> putMain.put(query, value).let { putCache.put(query, it) }
    is CacheSyncOperation -> putCache.put(query, value).let { putMain.put(query, it) }
    else -> throw OperationNotAllowedException()
  }

  override suspend fun putAll(query: Query, value: List<V>?, operation: Operation): List<V> = when (operation) {
    is DefaultOperation -> putAll(query, value, MainSyncOperation)
    is MainOperation -> putMain.putAll(query, value)
    is CacheOperation -> putCache.putAll(query, value)
    is MainSyncOperation -> putMain.putAll(query, value).let { putCache.putAll(query, it) }
    is CacheSyncOperation -> putCache.putAll(query, value).let { putMain.putAll(query, it) }
    else -> throw OperationNotAllowedException()
  }

  override suspend fun delete(query: Query, operation: Operation): Unit = when (operation) {
    is DefaultOperation -> delete(query, MainSyncOperation)
    is MainOperation -> deleteMain.delete(query)
    is CacheOperation -> deleteCache.delete(query)
    is MainSyncOperation -> deleteMain.delete(query).let { deleteCache.delete(query) }
    is CacheSyncOperation -> deleteCache.delete(query).let { deleteMain.delete(query) }
    else -> throw OperationNotAllowedException()
  }

  /**
   *  Default implementation returns always true (all objects are valid)
   */
  class DefaultValidator<T> : Validator<T> {
    override fun isValid(value: T): Boolean {
      return true
    }
  }
}
