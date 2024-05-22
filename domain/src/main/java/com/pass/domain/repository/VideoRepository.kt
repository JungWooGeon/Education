package com.pass.domain.repository

import kotlinx.coroutines.flow.Flow

interface VideoRepository {
    fun createVideoThumbnail(videoUri: String): Result<String>
    suspend fun addVideo(videoUri: String, videoThumbnailBitmap: String, title: String): Flow<Result<Boolean>>
}