package com.mobilejazz.harmony.data.repository

import com.mobilejazz.harmony.data.datasource.DeleteDataSource
import com.mobilejazz.harmony.data.datasource.GetDataSource
import com.mobilejazz.harmony.data.datasource.PutDataSource
import com.mobilejazz.harmony.data.operation.Operation
import com.mobilejazz.harmony.data.query.Query

class SingleDataSourceRepository<T>(
  private val getDataSource: GetDataSource<T>,
  private val putDataSource: PutDataSource<T>,
  private val deleteDataSource: DeleteDataSource
) : GetRepository<T>, PutRepository<T>, DeleteRepository {

  override suspend fun get(query: Query, operation: Operation): T = getDataSource.get(query)

  override suspend fun getAll(query: Query, operation: Operation): List<T> = getDataSource.getAll(query)

  override suspend fun put(query: Query, value: T?, operation: Operation): T = putDataSource.put(query, value)

  override suspend fun putAll(query: Query, value: List<T>?, operation: Operation): List<T> = putDataSource.putAll(query, value)

  override suspend fun delete(query: Query, operation: Operation) = deleteDataSource.delete(query)
}

class SingleGetDataSourceRepository<T>(private val getDataSource: GetDataSource<T>) : GetRepository<T> {

  override suspend fun get(query: Query, operation: Operation): T = getDataSource.get(query)

  override suspend fun getAll(query: Query, operation: Operation): List<T> = getDataSource.getAll(query)
}

class SinglePutDataSourceRepository<T>(private val putDataSource: PutDataSource<T>) : PutRepository<T> {
  override suspend fun put(query: Query, value: T?, operation: Operation): T = putDataSource.put(query, value)

  override suspend fun putAll(query: Query, value: List<T>?, operation: Operation): List<T> = putDataSource.putAll(query, value)
}

class SingleDeleteDataSourceRepository(private val deleteDataSource: DeleteDataSource) : DeleteRepository {

  override suspend fun delete(query: Query, operation: Operation) = deleteDataSource.delete(query)
}
