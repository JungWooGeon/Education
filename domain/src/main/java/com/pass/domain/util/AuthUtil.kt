package com.pass.domain.util

import kotlinx.coroutines.flow.Flow

interface AuthUtil {
    suspend fun isSignedIn(): Flow<Boolean>
    suspend fun signIn(id: String, password: String): Flow<Result<Unit>>
    suspend fun signUp(id: String, password: String, verifyPassword: String): Flow<Result<String>>
    suspend fun signOut()
}