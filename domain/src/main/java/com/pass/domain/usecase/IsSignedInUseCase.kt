package com.pass.domain.usecase

import com.pass.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class IsSignedInUseCase @Inject constructor(private val profileRepository: ProfileRepository) {
    suspend operator fun invoke(): Flow<Boolean> {
        return profileRepository.isSignedIn()
    }
}