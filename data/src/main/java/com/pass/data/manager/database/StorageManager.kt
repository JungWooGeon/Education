package com.pass.data.manager.database

import kotlinx.coroutines.flow.Flow

interface StorageManager {
    suspend fun updateFile(fileUri: String, pathString: String): Flow<Result<String>>
    suspend fun updateFileWithBitmap(bitmapString: String, videoId: String): Flow<Result<String>>
    suspend fun deleteFile(pathString: String): Flow<Result<Unit>>
}