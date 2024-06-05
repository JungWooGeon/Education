package com.pass.domain.usecase

import com.pass.domain.repository.LiveStreamingRepository
import javax.inject.Inject

class StopViewingUseCase<T> @Inject constructor(private val liveStreamingRepository: LiveStreamingRepository<T>) {
    suspend operator fun invoke() {
        return liveStreamingRepository.stopViewing()
    }
}