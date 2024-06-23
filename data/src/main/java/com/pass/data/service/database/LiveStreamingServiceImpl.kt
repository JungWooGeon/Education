package com.pass.data.service.database

import android.graphics.Bitmap
import com.google.firebase.firestore.DocumentSnapshot
import com.pass.data.di.DateTimeProvider
import com.pass.data.manager.database.DatabaseManager
import com.pass.data.manager.database.StorageManager
import com.pass.data.util.CalculateUtil
import com.pass.data.util.FireStoreUtil
import com.pass.domain.model.LiveStreaming
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * LiveStreaming 관련 데이터 가공(생성, 수정, 추출 등) 및 비지니스 로직 구현
 */
class LiveStreamingServiceImpl @Inject constructor(
    firebaseDatabaseManager: DatabaseManager<DocumentSnapshot>,
    fireStoreUtil: FireStoreUtil,
    private val firebaseStorageManager: StorageManager,
    private val calculateUtil: CalculateUtil,
    private val dateTimeProvider: DateTimeProvider
) : LiveStreamingService, MediaBaseServiceImpl(firebaseDatabaseManager, fireStoreUtil) {

    /**
     * 1. Flow<Result<Pair<DocumentSnapshot, List<String>> : Firestore에서 전체 livestreaming list 조회 + id 추출 결과
     * 2. Flow<Result<DocumentSnapshot>> : id 추출 결과를 토대로 id user 정보 조회 결과
     * 3. resultLiveStreamingList: 1번과 2번의 결과를 조합하여 데이터 가공 후 return
     */
    override suspend fun getLiveStreamingList(): Flow<Result<List<LiveStreaming>>> = callbackFlow {
        val resultPair = getMediaAndIdList("liveStreams").first()

        resultPair.onSuccess { pair ->
            val videoDocumentSnapShotList = pair.first
            val idList = pair.second

            val resultIdList = getIdList(idList).first()
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
        }.onFailure {
            trySend(Result.failure(it))
        }

        awaitClose()
    }

    /**
     * 1. Flow<Result<String> : FireStorage에 썸네일 이미지 저장 후 URI 반환
     * 2. broadcastData : livestreaming 으로 추가할 데이터 생성
     * 3. Flow<Result<Unit>> : FireStore에 livestreaming 데이터 추가
     */
    override suspend fun createLiveStreamingData(broadcastId: String, title: String, thumbnailImage: Bitmap): Flow<Result<Unit>> = callbackFlow {
        val thumbnailResult = firebaseStorageManager.updateFileWithBitmap(thumbnailImage, "live_streaming_thumbnail/${broadcastId}").first()
        thumbnailResult.onSuccess {
            val liveStreamingThumbnailUri = thumbnailResult.getOrDefault("")

            val nowDateTime = dateTimeProvider.localDateTimeNowFormat()

            val broadcastData = fireStoreUtil.createBroadcastData(
                broadcastId = broadcastId,
                title = title,
                startTime = nowDateTime,
                liveThumbnailUri = liveStreamingThumbnailUri
            )

            val createDataResult = firebaseDatabaseManager.createData(
                dataMap = broadcastData,
                collectionPath = "liveStreams",
                documentPath = broadcastId
            ).first()
            trySend(createDataResult)
        }.onFailure {
            trySend(Result.failure(it))
        }

        awaitClose()
    }

    /**
     * livestreaming 데이터 삭제
     */
    override suspend fun deleteLiveStreamingData(broadcastId: String) {
        // firebase delete
        firebaseDatabaseManager.deleteData(
            collectionPath = "liveStreams",
            documentPath = broadcastId
        ).first()
    }
}