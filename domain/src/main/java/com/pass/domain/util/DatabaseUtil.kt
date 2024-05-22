package com.pass.domain.util

import kotlinx.coroutines.flow.Flow

interface DatabaseUtil<T> {
    suspend fun deleteData()
    suspend fun readData(): Flow<Result<T>>
    suspend fun createData(
        dataMap: HashMap<String, String>,
        collectionPath: String,
        documentPath: String,
        collectionPath2: String? = null,
        documentPath2: String? = null
    ): Flow<Result<Unit>>

    suspend fun updateData(name: String, field: String, collectionPath: String): Flow<Result<Unit>>
}