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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.zip
import javax.inject.Inject

/**
 * User 관련 데이터 가공(생성, 수정, 추출 등) 및 비지니스 로직 구현
 */
class UserServiceImpl @Inject constructor(
    private val firebaseDatabaseManager: DatabaseManager<DocumentSnapshot>,
    private val firebaseStorageManager: StorageManager,
    private val calculateUtil: CalculateUtil,
    private val fireStoreUtil: FireStoreUtil
) : UserService {

    /**
     * 1. Flow<Result<DocumentSnapshot>> : 프로필 정보 조회
     * 2. Flow<Result<List<DocumentSnapshot>>> : 내 프로필 동영상 목록 조회
     * 3. 1번과 2번 결과를 조합하여 Profile 데이터 생성 후 return
     */
    override suspend fun getUserProfile(userId: String): Flow<Result<Profile>> {
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
        return readProfileInfoFlow.zip(readProfileVideoListFlow) { readProfileInfoFlowResult, readProfileVideoListFlowResult ->
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
                else -> { Result.failure(readProfileVideoListFlowResult.exceptionOrNull() ?: Exception("알 수 없는 오류")) }
            }
        }
    }

    /**
     * Flow<Result<Unit>> : User 정보 수정
     */
    override suspend fun updateUserProfile(name: String, field: String): Flow<Result<Unit>> {
        return firebaseDatabaseManager.updateData(name, field, "profiles")
    }

    /**
     * 1. Flow<Result<String>> : firestorage 사진 파일 수정
     * 2. Flow<Result<Unit>> : firestore 사진 url 정보 업데이트
     */
    override suspend fun updateUserProfilePicture(userId: String, pictureUri: String): Flow<Result<String>> = callbackFlow {
        // 사진 업데이트 + 새로운 사진 url 업데이트
        val updateFileResult = firebaseStorageManager.updateFile(pictureUri, "user_profile/${userId}").first()
        updateFileResult.onSuccess { uriString ->
            val updateProfileResult = updateUserProfile(uriString, "pictureUrl").first()
            updateProfileResult.onSuccess {
                trySend(Result.success(uriString))
            }.onFailure {
                trySend(Result.failure(Exception("프로필 사진 업로드를 시도하던 중 오류가 발생하였습니다. 잠시 후 시도해주세요.")))
            }
        }.onFailure {
            trySend(Result.failure(it))
        }

        awaitClose()
    }

    /**
     * Flow<Result<DocumentSnapshot>> : firestore 전체 사용자 정보 조회
     */
    override suspend fun getOtherUserProfile(userId: String): Flow<Result<Profile>> = callbackFlow {
        val readDataResult = firebaseDatabaseManager.readData(
            collectionPath =  "profiles",
            documentPath = userId
        ).first()

        readDataResult.onSuccess { documentSnapShot ->
            trySend(fireStoreUtil.createProfileFromDocumentSnapShot(documentSnapShot))
        }.onFailure {
            trySend(Result.failure(it))
        }

        awaitClose()
    }
}