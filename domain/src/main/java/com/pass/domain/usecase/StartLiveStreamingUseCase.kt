package com.pass.domain.usecase

import com.pass.domain.repository.LiveStreamingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class StartLiveStreamingUseCase @Inject constructor(private val liveStreamingRepository: LiveStreamingRepository) {
    suspend operator fun invoke(): Flow<Result<Boolean>> {
        return liveStreamingRepository.startLiveStreamingUseCase()
    }
}