package com.pass.domain.repository

import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    suspend fun login(id: String, password: String): Flow<Boolean>
}