package com.pass.data.manager.webrtc

import com.pass.data.BuildConfig
import org.json.JSONObject
import org.webrtc.AudioTrack
import org.webrtc.DataChannel
import org.webrtc.IceCandidate
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.RtpTransceiver
import org.webrtc.VideoTrack
import javax.inject.Inject

class PeerConnectionManagerImpl @Inject constructor(
    private val factory: PeerConnectionFactory
) : PeerConnectionManager {

    private var peerConnection: PeerConnection? = null

    override fun createPeerConnection(callbackOnIceCandidate: (JSONObject) -> Unit, callbackOnReceiveVideoTrack: (VideoTrack) -> Unit, callbackOnFailure: () -> Unit) {
        val iceServers = listOf(
            PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer(),
            PeerConnection.IceServer.builder(BuildConfig.turnServer).setUsername(BuildConfig.turnServerUserName).setPassword(
                BuildConfig.turnServerUserPassword).createIceServer()
        )
        val rtcConfig = PeerConnection.RTCConfiguration(iceServers).apply {
            sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
        }

        peerConnection = factory.createPeerConnection(rtcConfig, object : PeerConnection.Observer {
            override fun onIceCandidate(candidate: IceCandidate) {
                val json = JSONObject().put("candidate", candidate.sdp).put("sdpMid", candidate.sdpMid).put("sdpMLineIndex", candidate.sdpMLineIndex)
                callbackOnIceCandidate(json)
            }

            override fun onTrack(transceiver: RtpTransceiver?) {
                super.onTrack(transceiver)

                if (transceiver?.receiver?.track()?.kind() == "video") {
                    val videoTrack = (transceiver.receiver?.track() as VideoTrack).apply { setEnabled(true) }
                    callbackOnReceiveVideoTrack(videoTrack)
                }
            }

            override fun onAddStream(stream: MediaStream) {}
            override fun onIceConnectionChange(newState: PeerConnection.IceConnectionState?) {}
            override fun onSignalingChange(state: PeerConnection.SignalingState?) {
                if (state == PeerConnection.SignalingState.CLOSED) { callbackOnFailure() }
            }
            override fun onIceConnectionReceivingChange(p0: Boolean) {}
            override fun onIceGatheringChange(state: PeerConnection.IceGatheringState?) {}
            override fun onIceCandidatesRemoved(p0: Array<out IceCandidate>?) {}
            override fun onRemoveStream(p0: MediaStream?) {}
            override fun onDataChannel(p0: DataChannel?) {}
            override fun onRenegotiationNeeded() {}
        })
    }

    override fun addTrackPeerConnection(videoTrack: VideoTrack, audioTrack: AudioTrack) {
        peerConnection?.addTrack(videoTrack)
        peerConnection?.addTrack(audioTrack)
    }

    override fun disposePeerConnection() {
        try {
            peerConnection?.dispose()
        } catch(e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getPeerConnection(): PeerConnection? = peerConnection
}