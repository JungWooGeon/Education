package com.pass.data.service.database

import android.graphics.Bitmap
import com.pass.domain.model.Video
import kotlinx.coroutines.flow.Flow

interface VideoService {

    suspend fun addVideo(
        videoUri: String,
        videoThumbnailBitmap: Bitmap,
        title: String,
        userId: String
    ): Flow<Result<Unit>>

    suspend fun deleteVideo(video: Video, userId: String): Flow<Result<Unit>>

    suspend fun getAllVideoList(): Flow<Result<List<Video>>>
}