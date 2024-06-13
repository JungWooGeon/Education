package com.pass.data.service.webrtc

import org.json.JSONObject

interface WebRtcBroadCasterService {

    fun startBroadcast(broadcastId: String)

    fun stopBroadcast(broadcastId: String)

    fun handleAnswer(broadcastId: String, json: JSONObject)
}