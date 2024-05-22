package com.pass.domain.usecase

import com.pass.domain.repository.VideoRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AddVideoUseCase @Inject constructor(private val videoRepository: VideoRepository) {
    suspend operator fun invoke(videoUri: String, videoThumbnailBitmap: String, title: String): Flow<Result<Boolean>> {
        return videoRepository.addVideo(videoUri, videoThumbnailBitmap, title)
    }
}