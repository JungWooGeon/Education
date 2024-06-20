package com.pass.domain.usecase

import com.pass.domain.repository.VideoRepository
import javax.inject.Inject

class CreateVideoThumbnailUseCase<T> @Inject constructor(private val videoRepository: VideoRepository<T>) {
    operator fun invoke(videoUri: String): Result<T> {
        return videoRepository.createVideoThumbnail(videoUri)
    }
}