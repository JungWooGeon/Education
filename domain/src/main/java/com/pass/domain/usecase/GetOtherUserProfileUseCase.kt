package com.pass.domain.usecase

import com.pass.domain.model.Profile
import com.pass.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetOtherUserProfileUseCase @Inject constructor(private val profileRepository: ProfileRepository) {
    suspend operator fun invoke(userId: String) : Flow<Result<Profile>> {
        return profileRepository.getOtherUserProfile(userId)
    }
}