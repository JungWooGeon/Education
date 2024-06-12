package com.pass.data.manager.webrtc

import org.json.JSONObject
import org.webrtc.AddIceObserver
import org.webrtc.IceCandidate
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import javax.inject.Inject

class IceCandidateManagerImpl @Inject constructor(
    private val peerConnectionManager: PeerConnectionManager
): IceCandidateManager {

    override fun setRemoteDescriptionPeerConnection(json: JSONObject, sessionDescriptionType: SessionDescription.Type, callbackOnSetSuccess: () -> Unit, callbackOnFailure: () -> Unit) {
        val sdp = SessionDescription(sessionDescriptionType, json.getString("sdp"))

        peerConnectionManager.getPeerConnection()?.setRemoteDescription(object : SdpObserver {
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

    override fun addIceCandidatePeerConnection(json: JSONObject) {
        val candidate = IceCandidate(
            json.getString("sdpMid"),
            json.getInt("sdpMLineIndex"),
            json.getString("candidate")
        )

        peerConnectionManager.getPeerConnection()?.addIceCandidate(candidate, object : AddIceObserver {
            override fun onAddSuccess() {}
            override fun onAddFailure(p0: String?) {}
        })
    }
}