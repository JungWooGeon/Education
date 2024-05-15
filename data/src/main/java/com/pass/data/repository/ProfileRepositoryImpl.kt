package com.pass.data.repository

import com.pass.domain.repository.ProfileRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class ProfileRepositoryImpl @Inject constructor(): ProfileRepository {
    override suspend fun login(id: String, password: String): Flow<Boolean> = callbackFlow {
        trySend(true)
        awaitClose()
    }
}