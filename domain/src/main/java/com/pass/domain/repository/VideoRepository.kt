package com.pass.domain.repository

import com.pass.domain.model.Video
import kotlinx.coroutines.flow.Flow

interface VideoRepository<T> {
    fun createVideoThumbnail(videoUri: String): Result<T>

    suspend fun addVideo(videoUri: String, videoThumbnailBitmap: T, title: String): Flow<Result<Unit>>

    suspend fun deleteVideo(video: Video): Flow<Result<Unit>>

    suspend fun getAllVideoList(): Flow<Result<List<Video>>>
}