package com.pass.data.util

import com.pass.data.manager.MediaDeviceManager
import com.pass.data.manager.SocketManager
import com.pass.data.manager.WebRtcManager
import org.json.JSONObject
import org.webrtc.VideoTrack
import javax.inject.Inject

open class WebRtcBaseUtil @Inject constructor(
    protected val webRtcManager: WebRtcManager,
    protected val socketManager: SocketManager,
    protected val mediaDeviceManager: MediaDeviceManager
) {
    var onFailureConnected: (() -> Unit)? = null
    var onSuccessConnected: ((VideoTrack) -> Unit)? = null

    protected fun handleRemoteIceCandidate(json: JSONObject) {
        webRtcManager.addIceCandidatePeerConnection(json)
    }

    protected fun handleError() {
        webRtcManager.disposePeerConnection()
        socketManager.disconnect()
        onFailureConnected?.invoke()
    }
}