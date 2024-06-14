package com.pass.data.service.webrtc

import com.pass.data.manager.socket.SocketConnectionManager
import com.pass.data.manager.socket.SocketMessageManager
import com.pass.data.manager.webrtc.IceCandidateManager
import com.pass.data.manager.webrtc.PeerConnectionManager
import com.pass.data.manager.webrtc.SdpManager
import org.json.JSONObject
import javax.inject.Inject

open class WebRtcBaseServiceImpl @Inject constructor(
    protected val peerConnectionManager: PeerConnectionManager,
    protected val iceCandidateManager: IceCandidateManager,
    protected val sdpManager: SdpManager,
    protected val socketConnectionManager: SocketConnectionManager,
    protected val socketMessageManager: SocketMessageManager
): WebRtcBaseService {
    override fun handleRemoteIceCandidate(json: JSONObject) {
        iceCandidateManager.addIceCandidatePeerConnection(json)
    }

    override fun handleError(callbackOnFailureConnected: (() -> Unit)) {
        peerConnectionManager.disposePeerConnection()
        socketConnectionManager.disconnect()
        callbackOnFailureConnected()
    }
}