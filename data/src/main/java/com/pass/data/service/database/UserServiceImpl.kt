package com.pass.data.service.database

import com.google.firebase.firestore.DocumentSnapshot
import com.pass.data.manager.database.DatabaseManager
import com.pass.data.manager.database.StorageManager
import com.pass.data.util.CalculateUtil
import com.pass.data.util.FireStoreUtil
import com.pass.domain.model.Profile
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.zip
import javax.inject.Inject

class UserServiceImpl @Inject constructor(
    private val firebaseDatabaseManager: DatabaseManager<DocumentSnapshot>,
    private val firebaseStorageManager: StorageManager,
    private val calculateUtil: CalculateUtil,
    private val fireStoreUtil: FireStoreUtil
) : UserService {

    override suspend fun getUserProfile(userId: String): Flow<Result<Profile>> = callbackFlow {
        // 1. 프로필 정보 조회
        // 2. 내 프로필 동영상 목록 조회
        // 1 ~ 2 플로우를 동시에 실행하여 모두 성공 시 성공으로 반환

        // 프로필 정보 조회
        val readProfileInfoFlow = firebaseDatabaseManager.readData(
            collectionPath = "profiles",
            documentPath = userId
        )

        // 내 프로필 동영상 목록 조회
        val readProfileVideoListFlow = firebaseDatabaseManager.readDataList(
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

                    val profileResult = fireStoreUtil.extractProfileFromProfileAndVideoDocuments(
                        userId = userId,
                        calculateAgoTime = { calculateUtil.calculateAgoTime(it) },
                        profileDocumentSnapshot = profileDocumentSnapshot,
                        videoDocumentSnapshotList = videoDocumentSnapshotList
                    )

                    profileResult
                }

                readProfileInfoFlowResult.isFailure -> { Result.failure(readProfileInfoFlowResult.exceptionOrNull() ?: Exception("알 수 없는 오류")) }
                readProfileVideoListFlowResult.isFailure -> { Result.failure(readProfileVideoListFlowResult.exceptionOrNull() ?: Exception("알 수 없는 오류")) }
                else -> { Result.failure(Exception("알 수 없는 오류")) }
            }
        }.collect { combinedResult ->
            trySend(combinedResult)
            close()
        }

        awaitClose()
    }

    override suspend fun updateUserProfile(name: String, field: String): Flow<Result<Unit>> {
        return firebaseDatabaseManager.updateData(name, field, "profiles")
    }

    override suspend fun updateUserProfilePicture(userId: String, pictureUri: String): Flow<Result<String>> = callbackFlow {
        // 사진 업데이트 + 새로운 사진 url 업데이트
        firebaseStorageManager.updateFile(pictureUri, "user_profile/${userId}").collect { result ->
            result.onSuccess { uriString ->
                updateUserProfile(uriString, "pictureUrl").collect {
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

    override suspend fun getOtherUserProfile(userId: String): Flow<Result<Profile>> = callbackFlow {
        firebaseDatabaseManager.readData(
            collectionPath =  "profiles",
            documentPath = userId
        ).collect { result ->
            result.onSuccess { documentSnapShot ->
                val name = documentSnapShot.getString("name")
                val pictureUrl = documentSnapShot.getString("pictureUrl")

                if (name != null && pictureUrl != null) {
                    val profile = Profile(
                        name = name,
                        pictureUrl = pictureUrl,
                        videoList = emptyList()
                    )

                    trySend(Result.success(profile))
                } else {
                    trySend(Result.failure(Exception("프로필을 조회할 수 없습니다.")))
                }
            }.onFailure {
                trySend(Result.failure(it))
            }
        }

        awaitClose()
    }
}