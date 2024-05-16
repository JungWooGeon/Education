package com.pass.domain.usecase

import com.pass.domain.repository.ProfileRepository
import javax.inject.Inject

class SignOutUseCase @Inject constructor(private val profileRepository: ProfileRepository) {
    suspend operator fun invoke() {
        return profileRepository.signOut()
    }
}