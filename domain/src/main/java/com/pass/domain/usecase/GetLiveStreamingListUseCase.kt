package com.pass.domain.usecase

import com.pass.domain.model.LiveStreaming
import com.pass.domain.repository.LiveStreamingRepository
import javax.inject.Inject

class GetLiveStreamingListUseCase @Inject constructor(private val liveStreamingRepository: LiveStreamingRepository) {
    operator fun invoke(): Result<LiveStreaming> {
        return liveStreamingRepository.getLiveStreamingListUseCase()
    }
}