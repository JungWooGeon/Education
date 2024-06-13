package com.pass.data.service.auth

import kotlinx.coroutines.flow.Flow

interface SignService {

    suspend fun isSignedIn(): Flow<Boolean>

    suspend fun signIn(id: String, password: String): Flow<Result<Unit>>

    suspend fun signUp(id: String, password: String, verifyPassword: String): Flow<Result<Unit>>

    suspend fun signOut()
}