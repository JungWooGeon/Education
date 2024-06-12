package com.pass.data.service.webrtc

import com.pass.data.manager.socket.SocketConnectionManager
import com.pass.data.manager.socket.SocketMessageManager
import com.pass.data.manager.webrtc.IceCandidateManager
import com.pass.data.manager.webrtc.PeerConnectionManager
import com.pass.data.manager.webrtc.SdpManager
import org.json.JSONObject
import org.webrtc.VideoTrack
import javax.inject.Inject

open class WebRtcBaseService @Inject constructor(
    protected val peerConnectionManager: PeerConnectionManager,
    protected val iceCandidateManager: IceCandidateManager,
    protected val sdpManager: SdpManager,
    protected val socketConnectionManager: SocketConnectionManager,
    protected val socketMessageManager: SocketMessageManager
) {
    var onFailureConnected: (() -> Unit)? = null
    var onSuccessConnected: ((VideoTrack) -> Unit)? = null

    protected fun handleRemoteIceCandidate(json: JSONObject) {
        iceCandidateManager.addIceCandidatePeerConnection(json)
    }

    protected fun handleError() {
        peerConnectionManager.disposePeerConnection()
        socketConnectionManager.disconnect()
        onFailureConnected?.invoke()
    }
}