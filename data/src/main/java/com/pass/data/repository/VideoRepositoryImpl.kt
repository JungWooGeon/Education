package com.pass.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.util.Base64
import androidx.core.net.toUri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.pass.data.di.DateTimeProvider
import com.pass.data.util.CalculateUtil
import com.pass.domain.model.Profile
import com.pass.domain.model.Video
import com.pass.domain.repository.VideoRepository
import com.pass.domain.util.DatabaseUtil
import com.pass.domain.util.StorageUtil
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.zip
import java.io.ByteArrayOutputStream
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import javax.inject.Inject

class VideoRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firebaseDatabaseUtil: DatabaseUtil<DocumentSnapshot>,
    private val firebaseStorageUtil: StorageUtil,
    private val context: Context,
    private val calculateUtil: CalculateUtil,
    private val mediaMetadataRetriever: MediaMetadataRetriever,
    private val byteArrayOutputStream: ByteArrayOutputStream,
    private val dateTimeProvider: DateTimeProvider
) : VideoRepository {
    override fun createVideoThumbnail(videoUri: String): Result<String> {
        try {
            // 미디어 파일로부터 데이터를 추출하기 위해 setDataSource를 호출
            mediaMetadataRetriever.setDataSource(context, URLDecoder.decode(videoUri, StandardCharsets.UTF_8.toString()).toUri())

            // 첫 번째 프레임을 비트맵으로 가져옴
            val thumbnailBitmap = mediaMetadataRetriever.frameAtTime

            return if (thumbnailBitmap == null) {
                Result.failure(Exception("동영상 선택에 실패하였습니다."))
            } else {
                Result.success(convertBitmapToString(thumbnailBitmap))
            }
        } catch (e: IllegalArgumentException) {
            return Result.failure(e)
        } finally {
            // MediaMetadataRetriever 인스턴스를 해제
            mediaMetadataRetriever.release()
        }
    }

    override suspend fun addVideo(
        videoUri: String,
        videoThumbnailBitmap: String,
        title: String
    ): Flow<Result<Boolean>> = callbackFlow {
        // 1. 동영상 업로드
        // 2. 동영상 썸네일 이미지 업로드
        // 3. 전체 비디오 목록에 추가
        // 4. 내 프로필에 내 비디오 목록에 추가
        // 1 ~ 2 번 동기적으로 성공 시 3 ~ 4 번 병렬적으로 진행 후 모두 성공할 경우에만 성공으로 반환

        val uid = auth.currentUser?.uid
        val nowDateTime = dateTimeProvider.localDateTimeNowFormat()
        val videoId = "${uid}_${nowDateTime}"

        if (uid == null) {
            trySend(Result.failure(Exception("오류가 발생하였습니다. 다시 로그인을 진행해주세요.")))
        } else {
            // 비디오 업로드
            firebaseStorageUtil.updateFile(videoUri, "video/${videoId}").collect { result ->
                result.onSuccess { videoUriString ->
                    // 비디오 썸네일 업로드
                    firebaseStorageUtil.updateFileWithBitmap(videoThumbnailBitmap, videoId)
                        .collect { thumbnailResult ->
                            thumbnailResult.onSuccess { videoThumbnailUri ->
                                // 내 비디오 목록에 추가
                                val profileVideoData = hashMapOf(
                                    "title" to title,
                                    "userId" to uid,
                                    "videoThumbnailUrl" to URLDecoder.decode(
                                        videoThumbnailUri,
                                        StandardCharsets.UTF_8.toString()
                                    ),
                                    "videoUrl" to videoUriString,
                                    "time" to nowDateTime
                                )

                                val profileVideoFlow = firebaseDatabaseUtil.createData(
                                    profileVideoData,
                                    "profiles",
                                    uid,
                                    "videos",
                                    videoId
                                )

                                // 전체 비디오 목록에 추가
                                val videoData = hashMapOf(
                                    "title" to title,
                                    "userId" to uid,
                                    "videoThumbnailUrl" to URLDecoder.decode(
                                        videoThumbnailUri,
                                        StandardCharsets.UTF_8.toString()
                                    ),
                                    "videoUrl" to videoUriString,
                                    "time" to nowDateTime
                                )

                                val allVideoFlow = firebaseDatabaseUtil.createData(
                                    videoData,
                                    "videos",
                                    videoId
                                )

                                // 두 flow 가 모두 성공했을 경우에만 Success
                                profileVideoFlow.zip(allVideoFlow) { profileVideoFlowResult, allVideoFlowResult ->
                                    when {
                                        profileVideoFlowResult.isSuccess && allVideoFlowResult.isSuccess -> {
                                            Result.success(true)
                                        }

                                        profileVideoFlowResult.isFailure -> {
                                            Result.failure(
                                                profileVideoFlowResult.exceptionOrNull()
                                                    ?: Exception("알 수 없는 오류")
                                            )
                                        }

                                        allVideoFlowResult.isFailure -> {
                                            Result.failure(
                                                allVideoFlowResult.exceptionOrNull()
                                                    ?: Exception("알 수 없는 오류")
                                            )
                                        }

                                        else -> {
                                            Result.failure(Exception("알 수 없는 오류"))
                                        }
                                    }
                                }.collect { combinedResult ->
                                    trySend(combinedResult)
                                    close()
                                }
                            }.onFailure { e ->
                                trySend(Result.failure(e))
                            }
                        }
                }.onFailure {
                    trySend(Result.failure(it))
                }
            }
        }

        awaitClose()
    }

    override suspend fun deleteVideo(video: Video): Flow<Result<Unit>> = callbackFlow {
        // 1. 동영상 원본 삭제
        // 2. 동영상 썸네일 삭제
        // 3. 내 프로필에서 동영상 목록
        // 4. 전체 동영상 목록에서 삭제
        // 1 ~ 4 번 병렬적 실행 후 모두 성공 시에만 성공으로 반환

        val uid = auth.currentUser?.uid

        if (uid == null) {
            trySend(Result.failure(Exception("오류가 발생하였습니다. 다시 로그인을 진행해주세요.")))
        } else {
            val deleteVideoFromStorageFlow = firebaseStorageUtil.deleteFile(
                pathString = "video/${video.videoId}"
            )

            val deleteVideoThumbnailFromStorageFlow = firebaseStorageUtil.deleteFile(
                pathString = "video_thumbnail/${video.videoId}"
            )

            val deleteVideoFromProfileFlow = firebaseDatabaseUtil.deleteData(
                collectionPath = "profiles",
                documentPath = uid,
                collectionPath2 = "videos",
                documentPath2 = video.videoId
            )

            val deleteVideoFromTotalVideoListFlow = firebaseDatabaseUtil.deleteData(
                collectionPath = "videos",
                documentPath = video.videoId
            )

            combine(
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
            }.collect { combinedResult ->
                trySend(combinedResult)
                close()
            }
        }

        awaitClose()
    }

    override suspend fun getAllVideoList(): Flow<Result<List<Video>>> = callbackFlow {
        // 비디오 목록 조회
        firebaseDatabaseUtil.readDataList("videos").collect { readDataListResult ->
            readDataListResult.onSuccess { videoDocumentSnapShotList ->
                val idSetList = mutableSetOf<String>()
                videoDocumentSnapShotList.forEach { videoDocumentSnapShot ->
                    val userId = videoDocumentSnapShot.getString("userId")
                    userId?.let { idSetList.add(it) }
                }

                // id 목록 조회
                firebaseDatabaseUtil.readIdList(idSetList.toList()).collect { readIdListResult ->
                    readIdListResult.onSuccess { readIdDocumentSnapShotList ->
                        // id에 대한 프로필 정보 변수
                        val idOfVideoMap = mutableMapOf<String, Profile>()

                        // 어떤 id 목록이 있는지 조회
                        readIdDocumentSnapShotList.forEach { readIdDocumentSnapshot ->
                            val userId = readIdDocumentSnapshot.id
                            val name = readIdDocumentSnapshot.getString("name")
                            val pictureUrl = readIdDocumentSnapshot.getString("pictureUrl")

                            if (name != null && pictureUrl != null) {
                                idOfVideoMap[userId] = Profile(name, pictureUrl, emptyList())
                            }
                        }

                        // 결과 video list
                        val resultVideoList = mutableListOf<Video>()

                        // user 정보를 포함하여 video 정보 반영
                        videoDocumentSnapShotList.forEach { videoDocumentSnapShot ->
                            val videoId = videoDocumentSnapShot.id
                            val userId = videoDocumentSnapShot.getString("userId")
                            val title = videoDocumentSnapShot.getString("title")
                            val videoThumbnailUrl = videoDocumentSnapShot.getString("videoThumbnailUrl")
                            val videoUrl = videoDocumentSnapShot.getString("videoUrl")
                            val userName = idOfVideoMap[userId]?.name
                            val userProfileUrl = idOfVideoMap[userId]?.pictureUrl

                            val time = videoDocumentSnapShot.getString("time")
                            val agoTime = calculateUtil.calculateAgoTime(time)

                            if (userId != null && title != null && videoThumbnailUrl != null && videoUrl != null) {
                                resultVideoList.add(
                                    Video(
                                        videoId = videoId,
                                        userId = userId,
                                        videoThumbnailUrl = videoThumbnailUrl,
                                        videoTitle = title,
                                        agoTime = agoTime,
                                        videoUrl = videoUrl,
                                        userName = userName,
                                        userProfileUrl = userProfileUrl
                                    )
                                )
                            }
                        }

                        // return 결과 list
                        trySend(Result.success(resultVideoList))

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

    private fun convertBitmapToString(bitmap: Bitmap): String {
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }
}