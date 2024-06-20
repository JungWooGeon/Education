package com.pass.data.manager.database

import android.graphics.Bitmap
import kotlinx.coroutines.flow.Flow

interface StorageManager {
    suspend fun updateFile(fileUri: String, pathString: String): Flow<Result<String>>
    suspend fun updateFileWithBitmap(bitmap: Bitmap, pathString: String): Flow<Result<String>>
    suspend fun deleteFile(pathString: String): Flow<Result<Unit>>
}