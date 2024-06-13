package com.pass.data.manager.webrtc

import org.webrtc.MediaConstraints
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import javax.inject.Inject

class SdpManagerImpl @Inject constructor(
    private val peerConnectionManager: PeerConnectionManager
) : SdpManager {

    override fun createOfferPeerConnection(callbackOnSetSuccess: (String?) -> Unit, callbackOnFailure: () -> Unit) {
        peerConnectionManager.getPeerConnection()?.createOffer(object : SdpObserver {
            override fun onCreateSuccess(sessionDescription: SessionDescription) {
                peerConnectionManager.getPeerConnection()?.setLocalDescription(this, sessionDescription)
            }

            override fun onSetSuccess() {
                callbackOnSetSuccess(peerConnectionManager.getPeerConnection()?.localDescription?.description)
            }

            override fun onCreateFailure(p0: String?) {
                callbackOnFailure()
            }

            override fun onSetFailure(p0: String?) {
                callbackOnFailure()
            }
        }, MediaConstraints())
    }

    override fun createAnswerPeerConnection(callbackOnSetSuccess: (String?) -> Unit, callbackOnFailure: () -> Unit) {
        peerConnectionManager.getPeerConnection()?.createAnswer(object : SdpObserver {
            override fun onCreateSuccess(sessionDescription: SessionDescription) {
                peerConnectionManager.getPeerConnection()?.setLocalDescription(this, sessionDescription)
            }

            override fun onSetSuccess() {
                callbackOnSetSuccess(peerConnectionManager.getPeerConnection()?.localDescription?.description)
            }

            override fun onCreateFailure(p0: String?) {
                callbackOnFailure()
            }
            override fun onSetFailure(p0: String?) {
                callbackOnFailure()
            }
        }, MediaConstraints())
    }
}