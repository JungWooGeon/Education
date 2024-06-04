package com.pass.domain.repository

import com.pass.domain.model.LiveStreaming
import kotlinx.coroutines.flow.Flow

interface LiveStreamingRepository {
    suspend fun getLiveStreamingListUseCase(): Flow<Result<List<LiveStreaming>>>
    suspend fun startLiveStreamingUseCase(): Flow<Result<Boolean>>
}