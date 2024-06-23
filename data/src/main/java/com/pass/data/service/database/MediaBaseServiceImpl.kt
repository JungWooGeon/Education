package com.pass.data.service.database

import com.google.firebase.firestore.DocumentSnapshot
import com.pass.data.manager.database.DatabaseManager
import com.pass.data.util.FireStoreUtil
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

open class MediaBaseServiceImpl @Inject constructor(
    protected val firebaseDatabaseManager: DatabaseManager<DocumentSnapshot>,
    protected val fireStoreUtil: FireStoreUtil
) : MediaBaseService {

    /**
     * Flow<Result<Pair<DocumentSnapshot, List<String>> : Firestore에서 전체 Video or LiveStreaming list 조회 + id 추출 결과
     */
    override suspend fun getMediaAndIdList(collectionPath: String): Flow<Result<Pair<List<DocumentSnapshot>, List<String>>>> = callbackFlow {
        // 라이브 목록 조회
        val readDataListResult = firebaseDatabaseManager.readDataList(collectionPath).first()
        readDataListResult.onSuccess { videoDocumentSnapShotList ->
            // idList 추출
            val idList = fireStoreUtil.extractUserIdsFromDocuments(videoDocumentSnapShotList)
            trySend(Result.success(videoDocumentSnapShotList to idList))
        }.onFailure {
            trySend(Result.failure(it))
        }

        awaitClose()
    }

    /**
     * Flow<Result<DocumentSnapshot>> : id 추출 결과를 토대로 id user 정보 조회 결과
     */
    override suspend fun getIdList(idList: List<String>): Flow<Result<List<DocumentSnapshot>>> = callbackFlow {
        // id 목록 조회
        val readIdListResult = firebaseDatabaseManager.readIdList(idList).first()
        readIdListResult.onSuccess { readIdDocumentSnapShotList ->
            trySend(Result.success(readIdDocumentSnapShotList))
        }.onFailure {
            trySend(Result.failure(it))
        }

        awaitClose()
    }
}