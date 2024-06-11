package com.pass.data.manager

import com.pass.data.BuildConfig
import org.json.JSONObject
import org.webrtc.AddIceObserver
import org.webrtc.AudioTrack
import org.webrtc.DataChannel
import org.webrtc.IceCandidate
import org.webrtc.MediaConstraints
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.RtpTransceiver
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import org.webrtc.VideoTrack
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebRtcManager @Inject constructor(
    private val factory: PeerConnectionFactory
) {

    private var peerConnection: PeerConnection? = null

    fun createPeerConnection(callbackOnIceCandidate: (JSONObject) -> Unit, callbackOnReceiveVideoTrack: (VideoTrack) -> Unit, callbackOnFailure: () -> Unit) {
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
                } else {
                    // TODO Audio Track
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

    fun addTrackPeerConnection(videoTrack: VideoTrack, audioTrack: AudioTrack) {
        peerConnection?.addTrack(videoTrack)
        peerConnection?.addTrack(audioTrack)
    }

    fun disposePeerConnection() {
        try {
            peerConnection?.dispose()
        } catch(e: Exception) {
            e.printStackTrace()
        }
    }

    fun createOfferPeerConnection(callbackOnSetSuccess: (String?) -> Unit, callbackOnFailure: () -> Unit) {
        peerConnection?.createOffer(object : SdpObserver {
            override fun onCreateSuccess(sessionDescription: SessionDescription) {
                peerConnection?.setLocalDescription(this, sessionDescription)
            }

            override fun onSetSuccess() {
                callbackOnSetSuccess(peerConnection?.localDescription?.description)
            }

            override fun onCreateFailure(p0: String?) {
                callbackOnFailure()
            }

            override fun onSetFailure(p0: String?) {
                callbackOnFailure()
            }
        }, MediaConstraints())
    }

    fun createAnswerPeerConnection(callbackOnSetSuccess: (String?) -> Unit, callbackOnFailure: () -> Unit) {
        peerConnection?.createAnswer(object : SdpObserver {
            override fun onCreateSuccess(sessionDescription: SessionDescription) {
                peerConnection?.setLocalDescription(this, sessionDescription)
            }

            override fun onSetSuccess() {
                callbackOnSetSuccess(peerConnection?.localDescription?.description)
            }

            override fun onCreateFailure(p0: String?) {
                callbackOnFailure()
            }
            override fun onSetFailure(p0: String?) {
                callbackOnFailure()
            }
        }, MediaConstraints())
    }

    fun setRemoteDescriptionPeerConnection(json: JSONObject, sessionDescriptionType: SessionDescription.Type, callbackOnSetSuccess: () -> Unit, callbackOnFailure: () -> Unit) {
        val sdp = SessionDescription(sessionDescriptionType, json.getString("sdp"))

        peerConnection?.setRemoteDescription(object : SdpObserver {
            override fun onSetSuccess() {
                callbackOnSetSuccess()
            }

            override fun onCreateSuccess(p0: SessionDescription?) {}
            override fun onCreateFailure(p0: String?) {}
            override fun onSetFailure(p0: String?) {
                callbackOnFailure()
            }
        }, sdp)
    }

    fun addIceCandidatePeerConnection(json: JSONObject) {
        val candidate = IceCandidate(
            json.getString("sdpMid"),
            json.getInt("sdpMLineIndex"),
            json.getString("candidate")
        )

        peerConnection?.addIceCandidate(candidate, object : AddIceObserver {
            override fun onAddSuccess() {}
            override fun onAddFailure(p0: String?) {}
        })
    }
}