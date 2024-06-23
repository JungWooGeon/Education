package com.pass.data.service.database

import android.graphics.Bitmap
import com.google.firebase.firestore.DocumentSnapshot
import com.pass.data.di.DateTimeProvider
import com.pass.data.manager.database.DatabaseManager
import com.pass.data.manager.database.StorageManager
import com.pass.data.util.CalculateUtil
import com.pass.data.util.FireStoreUtil
import com.pass.domain.model.Video
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.zip
import javax.inject.Inject

/**
 * Video 관련 데이터 가공(생성, 수정, 추출 등) 및 비지니스 로직 구현
 */
class VideoServiceImpl @Inject constructor(
    firebaseDatabaseManager: DatabaseManager<DocumentSnapshot>,
    fireStoreUtil: FireStoreUtil,
    private val firebaseStorageManager: StorageManager,
    private val dateTimeProvider: DateTimeProvider,
    private val calculateUtil: CalculateUtil
) : VideoService, MediaBaseServiceImpl(firebaseDatabaseManager, fireStoreUtil) {

    /**
     * 1. Flow<Result<String>> : firestorage 비디오 파일 업로드
     * 2. Flow<Result<String>> : firestorage 비디오 썸네일 업로드
     * 3. Flow<Result<Unit>> : firestore user 비디오 목록 추가
     * 4. Flow<Result<Unit>> : firestore 전체 비디오 목록 추가
     */
    override suspend fun addVideo(
        videoUri: String,
        videoThumbnailBitmap: Bitmap,
        title: String,
        userId: String
    ): Flow<Result<Unit>> = flow {

        val nowDateTime = dateTimeProvider.localDateTimeNowFormat()
        val videoId = "${userId}_${nowDateTime}"

        val uploadVideoFileFlow = firebaseStorageManager.updateFile(videoUri, "video/${videoId}")
        val uploadVideoThumbnailFileFlow = firebaseStorageManager.updateFileWithBitmap(videoThumbnailBitmap, "video_thumbnail/${videoId}")

        val result = uploadVideoFileFlow.zip(uploadVideoThumbnailFileFlow) { videoResult, thumbnailResult ->
                if (videoResult.isSuccess and thumbnailResult.isSuccess) {
                    val videoUriString = videoResult.getOrDefault("")
                    val videoThumbnailUri = thumbnailResult.getOrDefault("")

                    val videoData = fireStoreUtil.createVideoData(
                        title = title,
                        userId = userId,
                        videoThumbnailUri = videoThumbnailUri,
                        videoUrl = videoUriString,
                        time = nowDateTime
                    )

                    // user 비디오 목록 추가, 전체 비디오 목록 추가 flow
                    val profileVideoFlow = firebaseDatabaseManager.createData(
                        videoData,
                        "profiles",
                        userId,
                        "videos",
                        videoId
                    )
                    val allVideoFlow = firebaseDatabaseManager.createData(videoData, "videos", videoId)

                    profileVideoFlow.zip(allVideoFlow) { profileVideoFlowResult, allVideoFlowResult ->
                        if (profileVideoFlowResult.isSuccess && allVideoFlowResult.isSuccess) {
                            Result.success(Unit)
                        } else {
                            Result.failure(Exception("File upload Failed"))
                        }
                    }.first()
                } else {
                    Result.failure(Exception("File upload Failed"))
                }
            }.first()

        emit(result)
    }

    /**
     * 1. flow : 동영상 원본 파일 삭제
     * 2. flow : 동영상 썸네일 파일 삭제
     * 3. flow : 내 프로필 동영상 목록 삭제
     * 4. flow : 전체 동영상 목록에서 삭제
     * 1 ~ 4 번 병렬적 실행 후 모두 성공 시에만 성공으로 반환, 중간 실패 시 return
     */
    override suspend fun deleteVideo(video: Video, userId: String): Flow<Result<Unit>> = callbackFlow {
        val deleteVideoFromStorageFlow = firebaseStorageManager.deleteFile(
            pathString = "video/${video.videoId}"
        )

        val deleteVideoThumbnailFromStorageFlow = firebaseStorageManager.deleteFile(
            pathString = "video_thumbnail/${video.videoId}"
        )

        val deleteVideoFromProfileFlow = firebaseDatabaseManager.deleteData(
            collectionPath = "profiles",
            documentPath = userId,
            collectionPath2 = "videos",
            documentPath2 = video.videoId
        )

        val deleteVideoFromTotalVideoListFlow = firebaseDatabaseManager.deleteData(
            collectionPath = "videos",
            documentPath = video.videoId
        )

        val combinedResult = combine(
            deleteVideoFromStorageFlow,
            deleteVideoThumbnailFromStorageFlow,
            deleteVideoFromProfileFlow,
            deleteVideoFromTotalVideoListFlow
        ) { dvfStorageFlowResult, dvtfStorageFlowResult, dvfProfileFlowResult, dvfTotalVideoListFlowResult ->
            if (dvfStorageFlowResult.isSuccess && dvtfStorageFlowResult.isSuccess && dvfProfileFlowResult.isSuccess && dvfTotalVideoListFlowResult.isSuccess) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("동영상 삭제에 실패하였습니다."))
            }
        }.first()

        trySend(combinedResult)
        awaitClose()
    }

    /**
     * 1. Flow<Result<Pair<DocumentSnapshot, List<String>> : Firestore에서 전체 video list 조회 + id 추출 결과
     * 2. Flow<Result<DocumentSnapshot>> : id 추출 결과를 토대로 id user 정보 조회 결과
     * 3. resultLiveStreamingList: 1번과 2번의 결과를 조합하여 데이터 가공 후 return
     */
    override suspend fun getAllVideoList(): Flow<Result<List<Video>>> = callbackFlow {
        // 비디오 목록 조회
        val pairResult = getMediaAndIdList("videos").first()
        pairResult.onSuccess { pair ->
            val videoDocumentSnapShotList = pair.first
            val idSetList = pair.second

            val readIdListResult = getIdList(idSetList).first()
            readIdListResult.onSuccess { readIdDocumentSnapShotList ->
                // id에 대한 프로필 정보 변수
                val idOfProfileMap = fireStoreUtil.extractIdOfProfileMapFromDocuments(readIdDocumentSnapShotList)

                // 결과 video list
                val resultVideoList = fireStoreUtil.extractVideoListInfoFromIdMapAndDocuments(
                    videoDocumentSnapShotList = videoDocumentSnapShotList,
                    idOfProfileMap = idOfProfileMap,
                    calculateAgoTime = { calculateUtil.calculateAgoTime(it) }
                )

                // return 결과 list
                trySend(Result.success(resultVideoList))
            }.onFailure {
                trySend(Result.failure(it))
            }
        }.onFailure {
            trySend(Result.failure(it))
        }

        awaitClose()
    }
}