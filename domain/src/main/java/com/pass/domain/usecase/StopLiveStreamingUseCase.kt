package com.pass.domain.usecase

import com.pass.domain.repository.LiveStreamingRepository
import javax.inject.Inject

class StopLiveStreamingUseCase<T, V> @Inject constructor(private val liveStreamingRepository: LiveStreamingRepository<T, V>) {
    suspend operator fun invoke() {
        return liveStreamingRepository.stopLiveStreaming()
    }
}