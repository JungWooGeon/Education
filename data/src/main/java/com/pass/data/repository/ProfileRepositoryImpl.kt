package com.pass.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.pass.domain.model.Profile
import com.pass.domain.repository.ProfileRepository
import com.pass.domain.util.AuthUtil
import com.pass.domain.util.DatabaseUtil
import com.pass.domain.util.StorageUtil
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class ProfileRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firebaseAuthUtil: AuthUtil,
    private val firebaseDatabaseUtil: DatabaseUtil<DocumentSnapshot>,
    private val firebaseStorageUtil: StorageUtil
) : ProfileRepository {

    override suspend fun isSignedIn(): Flow<Boolean> {
        return firebaseAuthUtil.isSignedIn()
    }

    override suspend fun signIn(id: String, password: String): Flow<Result<Unit>> {
        return firebaseAuthUtil.signIn(id, password)
    }

    override suspend fun signUp(
        id: String,
        password: String,
        verifyPassword: String
    ): Flow<Result<Unit>> = callbackFlow {
        firebaseAuthUtil.signUp(id, password, verifyPassword).collect { result ->
            result.onSuccess { uid ->
                // create user profile in database
                val userProfile = hashMapOf(
                    "name" to uid,
                    "pictureUrl" to ""
                )

                firebaseDatabaseUtil.createData(userProfile, "profiles", uid).collect {
                    trySend(it)
                }
            }.onFailure {
                trySend(Result.failure(it))
            }
        }

        awaitClose()
    }

    override suspend fun signOut() {
        return firebaseAuthUtil.signOut()
    }

    override suspend fun getUserProfile(): Flow<Result<Profile>> = callbackFlow {
        firebaseDatabaseUtil.readData().collect { result ->
            result.onSuccess { document ->
                val user = document.toObject(Profile::class.java)

                if (user != null) {
                    trySend(Result.success(user))
                } else {
                    trySend(Result.failure(Exception("프로필을 조회할 수 없습니다.")))
                }
            }.onFailure {
                trySend(Result.failure(it))
            }
        }

        awaitClose()
    }

    override suspend fun updateUserProfile(name: String, field: String): Flow<Result<Unit>> {
        return firebaseDatabaseUtil.updateData(name, field, "profiles")
    }

    override suspend fun updateUserProfilePicture(pictureUri: String): Flow<Result<String>> =
        callbackFlow {
            // 사진 업데이트 + 새로운 사진 url 업데이트
            firebaseStorageUtil.updateFile(pictureUri, "user_profile/${auth.currentUser?.uid}").collect { result ->
                result.onSuccess { uriString ->
                    firebaseDatabaseUtil.updateData(uriString, "pictureUrl", "profiles").collect {
                        it.onSuccess {
                            trySend(Result.success(uriString))
                        }.onFailure {
                            trySend(Result.failure(Exception("프로필 사진 업로드를 시도하던 중 오류가 발생하였습니다. 잠시 후 시도해주세요.")))
                        }
                    }
                }.onFailure {
                    trySend(Result.failure(it))
                }
            }

            awaitClose()
        }
}