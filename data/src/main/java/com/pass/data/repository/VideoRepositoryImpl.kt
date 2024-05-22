package com.pass.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.util.Base64
import androidx.core.net.toUri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.pass.domain.repository.VideoRepository
import com.pass.domain.util.DatabaseUtil
import com.pass.domain.util.StorageUtil
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.zip
import java.io.ByteArrayOutputStream
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class VideoRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firebaseDatabaseUtil: DatabaseUtil<DocumentSnapshot>,
    private val firebaseStorageUtil: StorageUtil,
    private val context: Context
) : VideoRepository {
    override fun createVideoThumbnail(videoUri: String): Result<String> {
        val retriever = MediaMetadataRetriever()

        try {
            // 미디어 파일로부터 데이터를 추출하기 위해 setDataSource를 호출
            retriever.setDataSource(
                context,
                URLDecoder.decode(videoUri, StandardCharsets.UTF_8.toString()).toUri()
            )

            // 첫 번째 프레임을 비트맵으로 가져옴
            val thumbnailBitmap = retriever.frameAtTime

            return if (thumbnailBitmap == null) {
                Result.failure(Exception("동영상 선택에 실패하였습니다."))
            } else {
                Result.success(convertBitmapToString(thumbnailBitmap))
            }
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            return Result.failure(Exception("동영상 선택에 실패하였습니다."))
        } finally {
            // MediaMetadataRetriever 인스턴스를 해제
            retriever.release()
        }
    }

    override suspend fun addVideo(videoUri: String, videoThumbnailBitmap: String, title: String): Flow<Result<Boolean>> = callbackFlow {
        // 1. 동영상 업로드
        // 2. 동영상 썸네일 이미지 업로드
        // 3. 전체 비디오 목록 추가
        // 4. 내 프로필에 내 비디오 목록 추가
        // 1 ~ 2 번 동기적으로 성공 시 3 ~ 4 번 병렬적으로 진행 후 모두 성공할 경우에만 성공으로 반환

        val uid = auth.currentUser?.uid
        val nowDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
        val videoId = "${uid}_${nowDateTime}"

        if (uid == null) {
            trySend(Result.failure(Exception("오류가 발생하였습니다. 다시 로그인을 진행해주세요.")))
        } else {
            firebaseStorageUtil.updateFile(videoUri, "video/${videoId}").collect { result ->
                result.onSuccess { videoUriString ->
                    // 비디오 썸네일 업로드
                    firebaseStorageUtil.updateFileWithBitmap(videoThumbnailBitmap, videoId).collect { thumbnailResult ->
                        thumbnailResult.onSuccess { videoThumbnailUri ->
                            // 내 비디오 목록에 추가
                            val profileVideoData = hashMapOf(
                                "videoThumbnailUrl" to URLDecoder.decode(videoThumbnailUri, StandardCharsets.UTF_8.toString()),
                                "videoUrl" to videoUriString
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
                                "videoUrl" to videoUriString
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
                                        Result.failure(profileVideoFlowResult.exceptionOrNull() ?: Exception("알 수 없는 오류"))
                                    }
                                    allVideoFlowResult.isFailure -> {
                                        Result.failure(allVideoFlowResult.exceptionOrNull() ?: Exception("알 수 없는 오류"))
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

    private fun convertBitmapToString(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }
}