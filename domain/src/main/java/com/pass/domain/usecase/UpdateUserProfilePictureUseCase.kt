package com.pass.domain.usecase

import com.pass.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UpdateUserProfilePictureUseCase @Inject constructor(private val profileRepository: ProfileRepository) {
    suspend operator fun invoke(uri: String): Flow<Result<String>> {
        return profileRepository.updateUserProfilePicture(uri)
    }
}