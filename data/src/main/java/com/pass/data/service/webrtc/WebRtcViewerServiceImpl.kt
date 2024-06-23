package com.pass.data.service.webrtc

import com.pass.data.manager.socket.SocketConnectionManager
import com.pass.data.manager.socket.SocketMessageManager
import com.pass.data.manager.webrtc.IceCandidateManager
import com.pass.data.manager.webrtc.PeerConnectionManager
import com.pass.data.manager.webrtc.SdpManager
import org.json.JSONObject
import org.webrtc.SessionDescription
import org.webrtc.VideoTrack
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebRtcViewerServiceImpl @Inject constructor(
    peerConnectionManager: PeerConnectionManager,
    iceCandidateManager: IceCandidateManager,
    socketConnectionManager: SocketConnectionManager,
    socketMessageManager: SocketMessageManager,
    sdpManager: SdpManager,
): WebRtcBaseServiceImpl(peerConnectionManager, iceCandidateManager, sdpManager, socketConnectionManager, socketMessageManager), WebRtcViewerService {

    override fun startViewing(broadcastId: String, callbackOnFailureConnected: () -> Unit, callbackOnSuccessConnected: (VideoTrack) -> Unit) {
        // 소켓 초기화 (시청자용)
        socketConnectionManager.initializeSocket(
            isBroadCaster = false,
            handleOffer = { handleOffer(broadcastId = broadcastId, json = it, callbackOnFailureConnected = callbackOnFailureConnected) },
            handleAnswer = { },
            handleError = { handleError(callbackOnFailureConnected = callbackOnFailureConnected) },
            handleRemoteIceCandidate = { handleRemoteIceCandidate(it) }
        )

        // PeerConnection 초기화
        peerConnectionManager.createPeerConnection(
            callbackOnIceCandidate = { json ->
                // callbackOnIceCandidate, 시청자 : IceCandidate 생성 시 서버로 바로 보냄
                socketMessageManager.emitMessage("iceCandidate", broadcastId, iceCandidate = json)
            },
            callbackOnReceiveVideoTrack = { videoTrack ->
                callbackOnSuccessConnected(videoTrack)
            },
            callbackOnFailure = callbackOnFailureConnected
        )

        // 소켓 연결
        socketConnectionManager.connect(
            onEventConnect = {
                socketMessageManager.emitMessage("join", broadcastId)
            },
            callbackOnFailureConnected = callbackOnFailureConnected
        )
    }

    override fun stopViewing() {
        peerConnectionManager.disposePeerConnection {}
        socketConnectionManager.disconnect()
    }

    override fun handleOffer(broadcastId: String, json: JSONObject, callbackOnFailureConnected: () -> Unit) {
        iceCandidateManager.setRemoteDescriptionPeerConnection(
            json = json,
            sessionDescriptionType = SessionDescription.Type.OFFER,
            callbackOnSetSuccess = {
                // callbackOnSetSuccess, offer 적용 완료 후 answer 생성
                sdpManager.createAnswerPeerConnection(
                    callbackOnSetSuccess = { peerConnectionLocalDescription ->
                        // callbackOnSetSuccess, answer 생성 후 서버로 전송
                        socketMessageManager.emitMessage("answer", broadcastId, peerConnectionLocalDescription)
                    },
                    callbackOnFailure = callbackOnFailureConnected
                )
            },
            callbackOnFailure = callbackOnFailureConnected
        )
    }
}