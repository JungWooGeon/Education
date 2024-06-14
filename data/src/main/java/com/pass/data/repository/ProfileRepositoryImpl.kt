package com.pass.data.repository

import com.pass.data.service.auth.AuthenticationService
import com.pass.data.service.auth.SignService
import com.pass.data.service.database.UserService
import com.pass.domain.model.Profile
import com.pass.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class ProfileRepositoryImpl @Inject constructor(
    private val signService: SignService,
    private val userService: UserService,
    private val authenticationService: AuthenticationService
) : ProfileRepository {

    override suspend fun isSignedIn(): Flow<Boolean> {
        return signService.isSignedIn()
    }

    override suspend fun signIn(id: String, password: String): Flow<Result<Unit>> {
        return signService.signIn(id, password)
    }

    override suspend fun signUp(id: String, password: String, verifyPassword: String): Flow<Result<Unit>> {
        return signService.signUp(id, password, verifyPassword)
    }

    override suspend fun signOut() {
        return signService.signOut()
    }

    override suspend fun getUserProfile(): Flow<Result<Profile>> {
        val userId = authenticationService.getCurrentUserId()

        return if (userId != null) {
            userService.getUserProfile(userId)
        } else {
            flowOf(Result.failure(Exception("오류가 발생하였습니다. 다시 로그인을 진행해주세요.")))
        }
    }

    override suspend fun updateUserProfile(name: String, field: String): Flow<Result<Unit>> {
        return userService.updateUserProfile(name, field)
    }

    override suspend fun updateUserProfilePicture(pictureUri: String): Flow<Result<String>> {
        val userId = authenticationService.getCurrentUserId()

        return if (userId != null) {
            userService.updateUserProfilePicture(
                userId = userId,
                pictureUri = pictureUri
            )
        } else {
            flowOf(Result.failure(Exception("오류가 발생하였습니다. 다시 로그인을 진행해주세요.")))
        }
    }

    override suspend fun getOtherUserProfile(userId: String): Flow<Result<Profile>> {
        return userService.getOtherUserProfile(userId)
    }
}