package com.pass.domain.usecase

import com.pass.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LoginUseCase @Inject constructor(private val profileRepository: ProfileRepository) {
    suspend operator fun invoke(id: String, password: String): Flow<Boolean> {
        return profileRepository.login(id, password)
    }
}