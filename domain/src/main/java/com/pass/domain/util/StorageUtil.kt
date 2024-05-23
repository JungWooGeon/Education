package com.pass.domain.util

import kotlinx.coroutines.flow.Flow

interface StorageUtil {
    suspend fun updateFile(fileUri: String, pathString: String): Flow<Result<String>>
    suspend fun updateFileWithBitmap(bitmapString: String, videoId: String): Flow<Result<String>>
    suspend fun deleteFile(pathString: String): Flow<Result<Unit>>
}