package com.pass.data.service.webrtc

import org.json.JSONObject
import org.webrtc.VideoTrack

interface WebRtcViewerService {

    fun startViewing(broadcastId: String, callbackOnFailureConnected: () -> Unit, callbackOnSuccessConnected: (VideoTrack) -> Unit)

    fun stopViewing()
}