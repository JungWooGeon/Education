package com.pass.domain.repository

import com.pass.domain.model.LiveStreaming
import kotlinx.coroutines.flow.Flow

interface LiveStreamingRepository<T> {
    suspend fun getLiveStreamingList(): Flow<Result<List<LiveStreaming>>>
    suspend fun startLiveStreaming(title: String): Flow<Result<Boolean>>
    suspend fun showLiveStreaming(broadcastId: String): Flow<Result<T>>
    suspend fun stopLiveStreaming()
}