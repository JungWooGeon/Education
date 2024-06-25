package com.pass.data.service.webrtc

import org.webrtc.VideoTrack

interface WebRtcBroadCasterService {

    fun startBroadcast(broadcastId: String, callbackOnFailureConnected: () -> Unit, callbackOnSuccessConnected: (VideoTrack) -> Unit)

    fun stopBroadcast(broadcastId: String)
}