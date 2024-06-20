package com.pass.domain.usecase

import com.pass.domain.repository.VideoRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AddVideoUseCase<T> @Inject constructor(private val videoRepository: VideoRepository<T>) {
    suspend operator fun invoke(videoUri: String, videoThumbnailBitmap: T, title: String): Flow<Result<Unit>> {
        return videoRepository.addVideo(videoUri, videoThumbnailBitmap, title)
    }
}