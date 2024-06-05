package com.pass.domain.usecase

import com.pass.domain.repository.LiveStreamingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class WatchLiveStreamingUseCase<T> @Inject constructor(private val liveStreamingRepository: LiveStreamingRepository<T>) {
    suspend operator fun invoke(broadcastId: String): Flow<Result<T>> {
        return liveStreamingRepository.watchLiveStreaming(broadcastId)
    }
}