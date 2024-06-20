package com.pass.data.service.database

import android.graphics.Bitmap
import com.pass.domain.model.LiveStreaming
import kotlinx.coroutines.flow.Flow

interface LiveStreamingService {

    suspend fun getLiveStreamingList(): Flow<Result<List<LiveStreaming>>>

    suspend fun createLiveStreamingData(broadcastId: String, title: String, thumbnailImage: Bitmap): Flow<Result<Unit>>

    suspend fun deleteLiveStreamingData(broadcastId: String)
}