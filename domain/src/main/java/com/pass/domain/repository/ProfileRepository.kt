package com.pass.domain.repository

import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    suspend fun isSignedIn(): Flow<Boolean>
    suspend fun signIn(id: String, password: String): Flow<Result<Unit>>
    suspend fun signUp(id: String, password: String): Flow<Result<Unit>>
    suspend fun signOut()
}