package com.pass.domain.repository

import com.pass.domain.model.LiveStreaming
import kotlinx.coroutines.flow.Flow

interface LiveStreamingRepository<T, V> {
    suspend fun getLiveStreamingList(): Flow<Result<List<LiveStreaming>>>
    suspend fun startLiveStreaming(title: String, thumbnailImage: V): Flow<Result<T>>
    suspend fun watchLiveStreaming(broadcastId: String): Flow<Result<T>>
    suspend fun stopLiveStreaming()
    suspend fun stopViewing()
}