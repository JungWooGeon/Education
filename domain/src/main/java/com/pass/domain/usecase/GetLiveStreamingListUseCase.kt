package com.pass.domain.usecase

import com.pass.domain.model.LiveStreaming
import com.pass.domain.repository.LiveStreamingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLiveStreamingListUseCase @Inject constructor(private val liveStreamingRepository: LiveStreamingRepository) {
    suspend operator fun invoke(): Flow<Result<List<LiveStreaming>>> {
        return liveStreamingRepository.getLiveStreamingListUseCase()
    }
}