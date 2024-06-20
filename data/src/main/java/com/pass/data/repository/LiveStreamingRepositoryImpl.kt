package com.pass.data.repository

import android.graphics.Bitmap
import com.pass.data.service.auth.AuthenticationService
import com.pass.data.service.database.LiveStreamingService
import com.pass.data.service.webrtc.WebRtcBroadCasterService
import com.pass.data.service.webrtc.WebRtcViewerService
import com.pass.domain.model.LiveStreaming
import com.pass.domain.repository.LiveStreamingRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.webrtc.VideoTrack
import javax.inject.Inject

class LiveStreamingRepositoryImpl @Inject constructor(
    private val webRtcBroadCasterService: WebRtcBroadCasterService,
    private val webRtcViewerService: WebRtcViewerService,
    private val liveStreamingService: LiveStreamingService,
    private val authenticationService: AuthenticationService
) : LiveStreamingRepository<VideoTrack, Bitmap> {

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

    override suspend fun startLiveStreaming(title: String, thumbnailImage: Bitmap): Flow<Result<VideoTrack>> = callbackFlow {
        // TODO cameraX 가 촬영하고 있다가 방송 시작하기를 누른 시점 캡처한 이미지를 썸네일 이미지로 저장 (presentation layer 에서 캡처 후 파라미터로 전달 필요)

        // 방송 시작 시 고유한 방송 ID 생성 및 Firebase에 저장
        val uid = authenticationService.getCurrentUserId()
        val broadcastId = uid.toString()

        if (uid == null) {
            trySend(Result.failure(Exception("오류가 발생하였습니다. 다시 로그인을 진행해주세요.")))
        } else {
            liveStreamingService.createLiveStreamingData(
                broadcastId = broadcastId,
                title = title,
                thumbnailImage = thumbnailImage
            ).collect { result ->
                result.onSuccess {
                    webRtcBroadCasterService.startBroadcast(
                        broadcastId = broadcastId,
                        callbackOnFailureConnected = {
                            trySend(Result.failure(Exception("Failed to connect to the broadcast."))).isFailure
                        },
                        callbackOnSuccessConnected = { videoTrack ->
                            trySend(Result.success(videoTrack))
                        }
                    )
                }.onFailure {
                    trySend(Result.failure(it))
                }
            }
        }

        awaitClose()
    }

    override suspend fun watchLiveStreaming(broadcastId: String): Flow<Result<VideoTrack>> = callbackFlow {
        // 방송 시청 시작
        webRtcViewerService.startViewing(
            broadcastId = broadcastId,
            callbackOnFailureConnected = {
                trySend(Result.failure(Exception("방송이 종료되었습니다."))).isFailure
            },
            callbackOnSuccessConnected = { videoTrack ->
                trySend(Result.success(videoTrack))
            }
        )

        awaitClose()
    }

    override suspend fun stopLiveStreaming() {
        val broadcastId = authenticationService.getCurrentUserId().toString()

        // delete broadcast data
        liveStreamingService.deleteLiveStreamingData(broadcastId)

        // webrtc release
        webRtcBroadCasterService.stopBroadcast(broadcastId)
    }

    override suspend fun stopViewing() {
        webRtcViewerService.stopViewing()
    }
}