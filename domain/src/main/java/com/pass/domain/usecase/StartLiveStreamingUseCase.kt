package com.pass.domain.usecase

import com.pass.domain.repository.LiveStreamingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class StartLiveStreamingUseCase<T, V> @Inject constructor(private val liveStreamingRepository: LiveStreamingRepository<T, V>) {
    suspend operator fun invoke(title: String, thumbnailImage: V): Flow<Result<T>> {
        return liveStreamingRepository.startLiveStreaming(title, thumbnailImage)
    }
}