package com.pass.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.pass.domain.model.Profile
import com.pass.domain.model.Video
import com.pass.domain.repository.ProfileRepository
import com.pass.domain.util.AuthUtil
import com.pass.domain.util.DatabaseUtil
import com.pass.domain.util.StorageUtil
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.zip
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
        // 1. 프로필 정보 조회
        // 2. 내 프로필 동영상 목록 조회
        // 1 ~ 2 플로우를 동시에 실행하여 모두 성공 시 성공으로 반환

        val userId = auth.currentUser?.uid

        if (userId != null) {
            // 프로필 정보 조회
            val readProfileInfoFlow = firebaseDatabaseUtil.readData(
                collectionPath =  "profiles",
                documentPath = userId
            )

            // 내 프로필 동영상 목록 조회
            val readProfileVideoListFlow = firebaseDatabaseUtil.readDataList(
                collectionPath = "profiles",
                documentPath = userId,
                collectionPath2 = "videos"
            )

            // 두 flow 가 모두 성공했을 경우에만 Success
            readProfileInfoFlow.zip(readProfileVideoListFlow) { readProfileInfoFlowResult, readProfileVideoListFlowResult ->
                when {
                    readProfileInfoFlowResult.isSuccess && readProfileVideoListFlowResult.isSuccess -> {
                        val profileDocumentSnapshot = readProfileInfoFlowResult.getOrNull()
                        val videoDocumentSnapshotList = readProfileVideoListFlowResult.getOrNull()

                        if (profileDocumentSnapshot == null || videoDocumentSnapshotList == null) {
                            Result.failure(Exception("프로필을 조회할 수 없습니다."))
                        } else {
                            val name = profileDocumentSnapshot.getString("name")
                            val pictureUrl = profileDocumentSnapshot.getString("pictureUrl")
                            val videoList = mutableListOf<Video>()

                            videoDocumentSnapshotList.forEach { videoDocumentSnapshot ->
                                val videoThumbnailUrl = videoDocumentSnapshot.getString("videoThumbnailUrl")
                                val videoUrl = videoDocumentSnapshot.getString("videoUrl")
                                val videoTitle = videoDocumentSnapshot.getString("title")
                                val videoIdSplitList = videoDocumentSnapshot.id.split("_")

                                if (videoThumbnailUrl != null && videoUrl != null && videoTitle != null && videoIdSplitList.size >= 2) {
                                    val time = videoIdSplitList[1]

                                    videoList.add(Video(
                                        videoId = videoDocumentSnapshot.id,
                                        userId = userId,
                                        videoThumbnailUrl = videoThumbnailUrl,
                                        time = time,
                                        videoTitle = videoTitle,
                                        videoUrl = videoUrl
                                    ))
                                }
                            }

                            if (name != null && pictureUrl != null) {
                                val profile = Profile(
                                    name = name,
                                    pictureUrl = pictureUrl,
                                    videoList = videoList
                                )

                                Result.success(profile)
                            } else {
                                Result.failure(Exception("프로필을 조회할 수 없습니다."))
                            }
                        }
                    }
                    readProfileInfoFlowResult.isFailure -> {
                        Result.failure(readProfileInfoFlowResult.exceptionOrNull() ?: Exception("알 수 없는 오류"))
                    }
                    readProfileVideoListFlowResult.isFailure -> {
                        Result.failure(readProfileVideoListFlowResult.exceptionOrNull() ?: Exception("알 수 없는 오류"))
                    }
                    else -> {
                        Result.failure(Exception("알 수 없는 오류"))
                    }
                }
            }.collect { combinedResult ->
                trySend(combinedResult)
                close()
            }
        } else {
            trySend(Result.failure(Exception("오류가 발생하였습니다. 다시 로그인을 진행해주세요.")))
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