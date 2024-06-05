package com.pass.domain.usecase

import com.pass.domain.model.LiveStreaming
import com.pass.domain.repository.LiveStreamingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLiveStreamingListUseCase<T> @Inject constructor(private val liveStreamingRepository: LiveStreamingRepository<T>) {
    suspend operator fun invoke(): Flow<Result<List<LiveStreaming>>> {
        return liveStreamingRepository.getLiveStreamingList()
    }
}