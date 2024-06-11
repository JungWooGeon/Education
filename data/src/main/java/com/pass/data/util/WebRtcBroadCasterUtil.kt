package com.pass.data.util

import com.pass.data.manager.MediaDeviceManager
import com.pass.data.manager.SocketManager
import com.pass.data.manager.WebRtcManager
import org.json.JSONObject
import org.webrtc.SessionDescription
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebRtcBroadCasterUtil @Inject constructor(
    webRtcManager: WebRtcManager,
    socketManager: SocketManager,
    mediaDeviceManager: MediaDeviceManager
): WebRtcBaseUtil(
    webRtcManager,
    socketManager,
    mediaDeviceManager
) {
    private var isBroadCasterConnected = false
    private val iceCandidateList = mutableListOf<JSONObject>()

    fun startBroadcast(broadcastId: String) {
        // 소켓 초기화 (방송자용)
        socketManager.initializeSocket(
            isBroadCaster = true,
            handleOffer = { },
            handleAnswer = { handleAnswer(broadcastId, it) },
            handleError = { handleError() },
            handleRemoteIceCandidate = { handleRemoteIceCandidate(it) }
        )

        // PeerConnection 초기화
        webRtcManager.createPeerConnection(
            callbackOnIceCandidate = { json ->
                // callbackOnIceCandidate, 방송자 : IceCandidate 생성 시 서버와 연결된 후에 보냄
                if (isBroadCasterConnected) {
                    socketManager.emitMessage("iceCandidate", broadcastId, iceCandidate = json)
                } else {
                    iceCandidateList.add(json)
                }
            },
            callbackOnReceiveVideoTrack = { videoTrack ->
                onSuccessConnected?.invoke(videoTrack)
            },
            callbackOnFailure = { onFailureConnected?.invoke() }
        )

        // 비디오 캡처 시작
        mediaDeviceManager.startVideoCapture { videoTrack, audioTrack ->
            // 캡처 완료한 VideoTrack 과 AudioTrack 을 PeerConnection 에 추가
            webRtcManager.addTrackPeerConnection(videoTrack, audioTrack)
        }

        // 소켓 연결
        socketManager.connect(onEventConnect = {
            webRtcManager.createOfferPeerConnection(
                callbackOnSetSuccess = { peerConnectionLocalDescription ->
                    // callbackOnSetSuccess
                    socketManager.emitMessage("start", broadcastId, sessionDescription = peerConnectionLocalDescription)
                },
                callbackOnFailure = { onFailureConnected?.invoke() }
            )
        })
    }

    fun stopBroadcast(broadcastId: String) {
        mediaDeviceManager.stopVideoCapture()
        webRtcManager.disposePeerConnection()
        socketManager.emitMessage("stop", broadcastId)
        socketManager.disconnect()
    }

    private fun handleAnswer(broadcastId: String, json: JSONObject) {
        webRtcManager.setRemoteDescriptionPeerConnection(
            json = json,
            sessionDescriptionType = SessionDescription.Type.ANSWER,
            callbackOnSetSuccess =  {
                // callbackOnSetSuccess, offer 적용 완료 후 저장한 IceCandidate 모두 전송
                iceCandidateList.forEach { iceCandidate ->
                    socketManager.emitMessage("iceCandidate", broadcastId, iceCandidate = iceCandidate)
                }
                iceCandidateList.clear()
                isBroadCasterConnected = true
            },
            callbackOnFailure = { onFailureConnected?.invoke() }
        )
    }
}