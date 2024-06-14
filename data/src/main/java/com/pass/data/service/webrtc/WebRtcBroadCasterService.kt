package com.pass.data.service.webrtc

import org.json.JSONObject
import org.webrtc.VideoTrack

interface WebRtcBroadCasterService {

    fun startBroadcast(broadcastId: String, callbackOnFailureConnected: () -> Unit, callbackOnSuccessConnected: (VideoTrack) -> Unit)

    fun stopBroadcast(broadcastId: String)

    fun handleAnswer(broadcastId: String, json: JSONObject, callbackOnFailureConnected: () -> Unit)
}