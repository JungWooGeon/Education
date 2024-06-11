package com.pass.data.util

import com.pass.data.manager.MediaDeviceManager
import com.pass.data.manager.SocketManager
import com.pass.data.manager.WebRtcManager
import org.json.JSONObject
import org.webrtc.SessionDescription
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebRtcViewerUtil @Inject constructor(
    webRtcManager: WebRtcManager,
    socketManager: SocketManager,
    mediaDeviceManager: MediaDeviceManager
): WebRtcBaseUtil(
    webRtcManager,
    socketManager,
    mediaDeviceManager
) {

    fun startViewing(broadcastId: String) {
        // 소켓 초기화 (시청자용)
        socketManager.initializeSocket(
            isBroadCaster = false,
            handleOffer = { handleOffer(broadcastId, it) },
            handleAnswer = { },
            handleError = { handleError() },
            handleRemoteIceCandidate = { handleRemoteIceCandidate(it) }
        )

        // PeerConnection 초기화
        webRtcManager.createPeerConnection(
            callbackOnIceCandidate = { json ->
                // callbackOnIceCandidate, 시청자 : IceCandidate 생성 시 서버로 바로 보냄
                socketManager.emitMessage("iceCandidate", broadcastId, iceCandidate = json)
            },
            callbackOnReceiveVideoTrack = { videoTrack ->
                onSuccessConnected?.invoke(videoTrack)
            },
            callbackOnFailure = { onFailureConnected?.invoke() }
        )

        // 소켓 연결
        socketManager.connect(onEventConnect = {
            socketManager.emitMessage("join", broadcastId)
        })
    }

    fun stopViewing() {
        webRtcManager.disposePeerConnection()
//        socket.emit("disconnect", broadcastId)
        socketManager.disconnect()
    }

    private fun handleOffer(broadcastId: String, json: JSONObject) {
        webRtcManager.setRemoteDescriptionPeerConnection(
            json = json,
            sessionDescriptionType = SessionDescription.Type.OFFER,
            callbackOnSetSuccess = {
                // callbackOnSetSuccess, offer 적용 완료 후 answer 생성
                webRtcManager.createAnswerPeerConnection(
                    callbackOnSetSuccess = { peerConnectionLocalDescription ->
                        // callbackOnSetSuccess, answer 생성 후 서버로 전송
                        socketManager.emitMessage("answer", broadcastId, peerConnectionLocalDescription)
                    },
                    callbackOnFailure = { onFailureConnected?.invoke() }
                )
            },
            callbackOnFailure = { onFailureConnected?.invoke() }
        )
    }
}