package com.pass.domain.usecase

import com.pass.domain.model.Video
import com.pass.domain.repository.VideoRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllVideoListUseCase<T> @Inject constructor(private val videoRepository: VideoRepository<T>) {
    suspend operator fun invoke(): Flow<Result<List<Video>>> {
        return videoRepository.getAllVideoList()
    }
}