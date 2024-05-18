package com.pass.domain.usecase

import com.pass.domain.model.Profile
import com.pass.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserProfileUseCase @Inject constructor(private val profileRepository: ProfileRepository) {
    suspend operator fun invoke(): Flow<Result<Profile>> {
        return profileRepository.getUserProfile()
    }
}