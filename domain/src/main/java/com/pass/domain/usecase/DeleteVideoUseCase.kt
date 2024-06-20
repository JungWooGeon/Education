package com.pass.domain.usecase

import com.pass.domain.model.Video
import com.pass.domain.repository.VideoRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DeleteVideoUseCase<T> @Inject constructor(private val videoRepository: VideoRepository<T>) {
    suspend operator fun invoke(video: Video): Flow<Result<Unit>> {
        return videoRepository.deleteVideo(video)
    }
}