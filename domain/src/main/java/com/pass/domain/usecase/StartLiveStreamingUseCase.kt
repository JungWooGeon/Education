package com.pass.domain.usecase

import com.pass.domain.repository.LiveStreamingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class StartLiveStreamingUseCase<T> @Inject constructor(private val liveStreamingRepository: LiveStreamingRepository<T>) {
    suspend operator fun invoke(title: String): Flow<Result<T>> {
        return liveStreamingRepository.startLiveStreaming(title)
    }
}