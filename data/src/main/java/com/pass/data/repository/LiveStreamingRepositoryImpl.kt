package com.pass.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.pass.data.service.database.LiveStreamingService
import com.pass.data.service.webrtc.WebRtcBroadCasterServiceImpl
import com.pass.data.service.webrtc.WebRtcViewerServiceImpl
import com.pass.domain.model.LiveStreaming
import com.pass.domain.repository.LiveStreamingRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.webrtc.VideoTrack
import javax.inject.Inject

class LiveStreamingRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val webRtcBroadCasterService: WebRtcBroadCasterServiceImpl,
    private val webRtcViewerService: WebRtcViewerServiceImpl,
    private val liveStreamingService: LiveStreamingService
) : LiveStreamingRepository<VideoTrack> {

    override suspend fun getLiveStreamingList(): Flow<Result<List<LiveStreaming>>> = callbackFlow {
        liveStreamingService.getLiveStreamingList().collect { resultLiveStreamingList ->
            resultLiveStreamingList.onSuccess { liveStreamingList ->
                trySend(Result.success(liveStreamingList))
            }.onFailure {
                trySend(Result.failure(it))
            }
        }

        awaitClose()
    }

    override suspend fun startLiveStreaming(title: String): Flow<Result<VideoTrack>> = callbackFlow {
        // TODO cameraX 가 촬영하고 있다가 방송 시작하기를 누른 시점 캡처한 이미지를 썸네일 이미지로 저장 (presentation layer 에서 캡처 후 파라미터로 전달 필요)

        // 방송 시작 시 고유한 방송 ID 생성 및 Firebase에 저장
        val uid = auth.currentUser?.uid
        val broadcastId = uid.toString()

        if (uid == null) {
            trySend(Result.failure(Exception("오류가 발생하였습니다. 다시 로그인을 진행해주세요.")))
        } else {
            liveStreamingService.createLiveStreamingData(
                broadcastId = broadcastId,
                title = title
            ).collect { result ->
                result.onSuccess {
                    // 연결 실패한 경우
                    webRtcBroadCasterService.onFailureConnected = {
                        trySend(Result.failure(Exception("Failed to connect to the broadcast."))).isFailure
                    }

                    // 연결 성공한 경우
                    webRtcBroadCasterService.onSuccessConnected = { videoTrack ->
                        trySend(Result.success(videoTrack))
                    }

                    webRtcBroadCasterService.startBroadcast(broadcastId)
                }.onFailure {
                    trySend(Result.failure(it))
                }
            }
        }

        awaitClose()
    }

    override suspend fun watchLiveStreaming(broadcastId: String): Flow<Result<VideoTrack>> = callbackFlow {
        // 방송 시청 시작
        webRtcViewerService.startViewing(broadcastId)

        // 연결 실패한 경우
        webRtcViewerService.onFailureConnected = {
            trySend(Result.failure(Exception("Failed to connect to the broadcast."))).isFailure
        }

        // 연결 성공한 경우
        webRtcViewerService.onSuccessConnected = { videoTrack ->
            trySend(Result.success(videoTrack))
        }

        awaitClose()
    }

    override suspend fun stopLiveStreaming() {
        val broadcastId = auth.currentUser?.uid.toString()

        // delete broadcast data
        liveStreamingService.deleteLiveStreamingData(broadcastId)

        // webrtc release
        webRtcBroadCasterService.stopBroadcast(broadcastId)
    }

    override suspend fun stopViewing() {
        webRtcViewerService.stopViewing()
    }
}