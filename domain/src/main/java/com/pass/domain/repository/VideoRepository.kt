package com.pass.domain.repository

import com.pass.domain.model.Video
import kotlinx.coroutines.flow.Flow

interface VideoRepository {
    fun createVideoThumbnail(videoUri: String): Result<String>

    suspend fun addVideo(videoUri: String, videoThumbnailBitmap: String, title: String): Flow<Result<Boolean>>

    suspend fun deleteVideo(video: Video): Flow<Result<Unit>>

    suspend fun getAllVideoList(): Flow<Result<List<Video>>>
}