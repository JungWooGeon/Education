package com.pass.domain.usecase

import com.pass.domain.repository.LiveStreamingRepository
import javax.inject.Inject

class StopLiveStreamingUseCase<T> @Inject constructor(private val liveStreamingRepository: LiveStreamingRepository<T>) {
    suspend operator fun invoke() {
        return liveStreamingRepository.stopLiveStreaming()
    }
}