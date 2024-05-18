package com.pass.domain.repository

import com.pass.domain.model.Profile
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    suspend fun isSignedIn(): Flow<Boolean>
    suspend fun signIn(id: String, password: String): Flow<Result<Unit>>
    suspend fun signUp(id: String, password: String, verifyPassword: String): Flow<Result<Unit>>
    suspend fun signOut()

    suspend fun getUserProfile(): Flow<Result<Profile>>
    suspend fun updateUserProfileName(name: String): Flow<Result<Unit>>
}