package com.pass.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.pass.data.di.DateTimeProvider
import com.pass.data.util.CalculateUtil
import com.pass.data.util.WebRtcUtil
import com.pass.domain.model.LiveStreaming
import com.pass.domain.model.Profile
import com.pass.domain.repository.LiveStreamingRepository
import com.pass.domain.util.DatabaseUtil
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.webrtc.VideoTrack
import javax.inject.Inject

class LiveStreamingRepositoryImpl @Inject constructor(
    private val webRtcUtil: WebRtcUtil,
    private val auth: FirebaseAuth,
    private val firebaseDatabaseUtil: DatabaseUtil<DocumentSnapshot>,
    private val dataTimeProvider: DateTimeProvider,
    private val calculateUtil: CalculateUtil
) : LiveStreamingRepository<VideoTrack> {

    override suspend fun getLiveStreamingList(): Flow<Result<List<LiveStreaming>>> = callbackFlow {
        // 라이브 목록 조회
        firebaseDatabaseUtil.readDataList("liveStreams").collect { readDataListResult ->
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
                        val resultLiveStreamingList = mutableListOf<LiveStreaming>()

                        // user 정보를 포함하여 video 정보 반영
                        videoDocumentSnapShotList.forEach { videoDocumentSnapShot ->
                            val broadcastId = videoDocumentSnapShot.id
                            val title = videoDocumentSnapShot.getString("title")
                            val startTime = videoDocumentSnapShot.getString("startTime")
                            val userId = videoDocumentSnapShot.getString("userId")
                            val userName = idOfVideoMap[userId]?.name
                            val userProfileUrl = idOfVideoMap[userId]?.pictureUrl
                            val time = videoDocumentSnapShot.getString("time")
                            val agoTime = calculateUtil.calculateAgoTime(time)
                            // TODO ThumbnailUrl 추가

                            if (userId != null && title != null && userProfileUrl != null && userName != null) {
                                resultLiveStreamingList.add(
                                    LiveStreaming(
                                        broadcastId = broadcastId,
                                        thumbnailURL = "",
                                        title = title,
                                        userProfileURL = userProfileUrl,
                                        userName = userName
                                    )
                                )
                            }
                        }

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

    override suspend fun startLiveStreaming(title: String): Flow<Result<Boolean>> = callbackFlow {
        // TODO cameraX 가 촬영하고 있다가 방송 시작하기를 누른 시점 캡처한 이미지를 썸네일 이미지로 저장 (presentation layer 에서 캡처 후 파라미터로 전달 필요)
        // TODO 방송 종료 시 firebase 에서 방송 목록 삭제

        // 방송 시작 시 고유한 방송 ID 생성 및 Firebase에 저장
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(Result.failure(Exception("오류가 발생하였습니다. 다시 로그인을 진행해주세요.")))
        } else {
            val broadcastId = uid.toString()
            val nowDateTime = dataTimeProvider.localDateTimeNowFormat()

            // TODO 추후 Thumbnail 추가
            val broadcastData = hashMapOf(
                "userId" to uid,
                "title" to title,
                "startTime" to nowDateTime
            )

            firebaseDatabaseUtil.createData(
                dataMap = broadcastData,
                collectionPath = "liveStreams",
                documentPath = broadcastId
            ).collect { result ->
                result.onSuccess {
                    try {
                        webRtcUtil.initializePeerConnectionFactory()
                        webRtcUtil.createPeerConnection()
                        webRtcUtil.createLocalMediaStream()
                        webRtcUtil.startBroadcast(broadcastId)

                        trySend(Result.success(true))
                    } catch (e: Exception) {
                        trySend(Result.failure(e))
                    }
                }.onFailure {
                    trySend(Result.failure(it))
                }
            }
        }

        awaitClose()
    }

    override suspend fun watchLiveStreaming(broadcastId: String): Flow<Result<VideoTrack>> = callbackFlow {
        webRtcUtil.initializePeerConnectionFactory()
        webRtcUtil.createPeerConnection()
        webRtcUtil.startViewing(broadcastId)

        if (webRtcUtil.onRemoteVideoTrackAvailable == null) {
            trySend(Result.failure(Exception("방송이 종료되었습니다.")))
        } else {
            webRtcUtil.onRemoteVideoTrackAvailable = { videoTrack ->
                trySend(Result.success(videoTrack))
            }
        }

        awaitClose()
    }

    override suspend fun stopLiveStreaming() {
        webRtcUtil.stopLiveStreaming()
    }

    override suspend fun stopViewing() {
        webRtcUtil.stopViewing()
    }
}