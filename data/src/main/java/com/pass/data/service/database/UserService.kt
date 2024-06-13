package com.pass.data.service.database

import com.pass.domain.model.Profile
import kotlinx.coroutines.flow.Flow

interface UserService {

    suspend fun getUserProfile(userId: String): Flow<Result<Profile>>

    suspend fun updateUserProfile(name: String, field: String): Flow<Result<Unit>>

    suspend fun updateUserProfilePicture(userId: String, pictureUri: String): Flow<Result<String>>

    suspend fun getOtherUserProfile(userId: String): Flow<Result<Profile>>
}