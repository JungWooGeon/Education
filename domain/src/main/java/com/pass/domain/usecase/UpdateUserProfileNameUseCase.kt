package com.pass.domain.usecase

import com.pass.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UpdateUserProfileNameUseCase @Inject constructor(private val profileRepository: ProfileRepository) {
    suspend operator fun invoke(name: String): Flow<Result<Unit>> {
        return profileRepository.updateUserProfile(name, "name")
    }
}