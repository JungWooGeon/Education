package com.pass.data.service.database

import com.google.firebase.firestore.DocumentSnapshot
import com.pass.data.di.DateTimeProvider
import com.pass.data.manager.database.DatabaseManager
import com.pass.data.util.CalculateUtil
import com.pass.data.util.FireStoreUtil
import com.pass.domain.model.LiveStreaming
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class LiveStreamingServiceImpl @Inject constructor(
    private val firebaseDatabaseManager: DatabaseManager<DocumentSnapshot>,
    private val calculateUtil: CalculateUtil,
    private val fireStoreUtil: FireStoreUtil,
    private val dateTimeProvider: DateTimeProvider
) : LiveStreamingService {

    override suspend fun getLiveStreamingList(): Flow<Result<List<LiveStreaming>>> = callbackFlow {
        getLiveStreamingAndIdList().collect { resultPair ->
            resultPair.onSuccess { pair ->
                val videoDocumentSnapShotList = pair.first
                val idList = pair.second

                getIdList(idList).collect { resultIdList ->
                    resultIdList.onSuccess { readIdDocumentSnapShotList ->
                        val idOfProfileMap = fireStoreUtil.extractIdOfProfileMapFromDocuments(readIdDocumentSnapShotList)

                        // 결과 video list
                        val resultLiveStreamingList = fireStoreUtil.extractLiveStreamingListInfoFromIdMapAndDocuments(
                            videoDocumentSnapShotList = videoDocumentSnapShotList,
                            idOfProfileMap = idOfProfileMap,
                            calculateAgoTime =  { calculateUtil.calculateAgoTime(it) }
                        )

                        // return 결과 list
                        trySend(Result.success(resultLiveStreamingList))
                    }.onFailure {
                        trySend(Result.failure(it))
                    }
                }
            }.onFailure {
                trySend(Result.failure(it))
            }
        }

        awaitClose()
    }

    override suspend fun createLiveStreamingData(broadcastId: String, title: String): Flow<Result<Unit>> = callbackFlow {
        val nowDateTime = dateTimeProvider.localDateTimeNowFormat()

        // TODO 추후 Thumbnail 추가
        val broadcastData = hashMapOf(
            "userId" to broadcastId,
            "title" to title,
            "startTime" to nowDateTime
        )

        firebaseDatabaseManager.createData(
            dataMap = broadcastData,
            collectionPath = "liveStreams",
            documentPath = broadcastId
        ).collect { result ->
            result.onSuccess {
                try {
                    trySend(Result.success(Unit))
                } catch (e: Exception) {
                    trySend(Result.failure(e))
                }
            }.onFailure {
                trySend(Result.failure(it))
            }
        }

        awaitClose()
    }

    override suspend fun deleteLiveStreamingData(broadcastId: String) {
        // firebase delete
        firebaseDatabaseManager.deleteData(
            collectionPath = "liveStreams",
            documentPath = broadcastId
        ).first()
    }

    private suspend fun getLiveStreamingAndIdList(): Flow<Result<Pair<List<DocumentSnapshot>, List<String>>>> = callbackFlow {
        // 라이브 목록 조회
        firebaseDatabaseManager.readDataList("liveStreams").collect { readDataListResult ->
            readDataListResult.onSuccess { videoDocumentSnapShotList ->
                // idList 추출
                val idList = fireStoreUtil.extractUserIdsFromDocuments(videoDocumentSnapShotList)
                trySend(Result.success(videoDocumentSnapShotList to idList))
            }.onFailure {
                trySend(Result.failure(it))
            }
        }

        awaitClose()
    }

    private suspend fun getIdList(idList: List<String>): Flow<Result<List<DocumentSnapshot>>> = callbackFlow {
        // id 목록 조회
        firebaseDatabaseManager.readIdList(idList).collect { readIdListResult ->
            readIdListResult.onSuccess { readIdDocumentSnapShotList ->
                trySend(Result.success(readIdDocumentSnapShotList))
            }.onFailure {
                trySend(Result.failure(it))
            }
        }

        awaitClose()
    }
}