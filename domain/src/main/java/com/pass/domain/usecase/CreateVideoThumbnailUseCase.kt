package com.pass.domain.usecase

import com.pass.domain.repository.VideoRepository
import javax.inject.Inject

class CreateVideoThumbnailUseCase @Inject constructor(private val videoRepository: VideoRepository) {
    operator fun invoke(videoUri: String): Result<String> {
        return videoRepository.createVideoThumbnail(videoUri)
    }
}