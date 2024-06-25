package com.pass.data.service.webrtc

import com.pass.data.manager.capture.AudioCaptureManager
import com.pass.data.manager.capture.VideoCaptureManager
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
class WebRtcBroadCasterServiceImpl @Inject constructor(
    peerConnectionManager: PeerConnectionManager,
    iceCandidateManager: IceCandidateManager,
    socketConnectionManager: SocketConnectionManager,
    socketMessageManager: SocketMessageManager,
    sdpManager: SdpManager,
    private val videoCaptureManager: VideoCaptureManager,
    private val audioCaptureManager: AudioCaptureManager,
): WebRtcBaseServiceImpl(peerConnectionManager, iceCandidateManager, sdpManager, socketConnectionManager, socketMessageManager), WebRtcBroadCasterService {

    private var isBroadCasterConnected = false
    private val iceCandidateList = mutableListOf<JSONObject>()

    override fun startBroadcast(broadcastId: String, callbackOnFailureConnected: () -> Unit, callbackOnSuccessConnected: (VideoTrack) -> Unit) {
        // 소켓 초기화 (방송자용)
        socketConnectionManager.initializeSocket(
            isBroadCaster = true,
            handleRemoteIceCandidate = { handleRemoteIceCandidate(it) },
            handleError = { handleError(callbackOnFailureConnected) },
            handleAnswer = { handleAnswer(broadcastId = broadcastId, json = it, callbackOnFailureConnected = callbackOnFailureConnected) },
            handleOffer = { }
        )

        // PeerConnection 초기화
        peerConnectionManager.createPeerConnection(
            callbackOnIceCandidate = { json ->
                // callbackOnIceCandidate, 방송자 : IceCandidate 생성 시 서버와 연결된 후에 보냄
                if (isBroadCasterConnected) {
                    socketMessageManager.emitMessage("iceCandidate", broadcastId, iceCandidate = json)
                } else {
                    iceCandidateList.add(json)
                }
            },
            callbackOnReceiveVideoTrack = { },
            callbackOnFailure = callbackOnFailureConnected
        )

        // 캡처 완료한 VideoTrack 과 AudioTrack 을 PeerConnection 에 추가
        val videoTrack = videoCaptureManager.startVideoCapture()
        val audioTrack = audioCaptureManager.startAudioCapture()

        callbackOnSuccessConnected(videoTrack)

        peerConnectionManager.addTrackPeerConnection(videoTrack, audioTrack)

        // 소켓 연결
        socketConnectionManager.connect(
            onEventConnect = {
                sdpManager.createOfferPeerConnection(
                    callbackOnSetSuccess = { peerConnectionLocalDescription ->
                        // callbackOnSetSuccess
                        socketMessageManager.emitMessage("start", broadcastId, sessionDescription = peerConnectionLocalDescription)
                    },
                    callbackOnFailure = callbackOnFailureConnected
                )
            },
            callbackOnFailureConnected = callbackOnFailureConnected
        )
    }

    override fun stopBroadcast(broadcastId: String) {
        videoCaptureManager.stopVideoCapture()
        socketMessageManager.emitMessage("stop", broadcastId)
        peerConnectionManager.disposePeerConnection {}
        socketConnectionManager.disconnect()
    }

    private fun handleAnswer(broadcastId: String, json: JSONObject, callbackOnFailureConnected: () -> Unit) {
        iceCandidateManager.setRemoteDescriptionPeerConnection(
            json = json,
            sessionDescriptionType = SessionDescription.Type.ANSWER,
            callbackOnSetSuccess =  {
                // callbackOnSetSuccess, offer 적용 완료 후 저장한 IceCandidate 모두 전송
                iceCandidateList.forEach { iceCandidate ->
                    socketMessageManager.emitMessage("iceCandidate", broadcastId, iceCandidate = iceCandidate)
                }
                iceCandidateList.clear()
                isBroadCasterConnected = true
            },
            callbackOnFailure = callbackOnFailureConnected
        )
    }
}