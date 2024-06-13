package com.pass.data.service.webrtc

import org.json.JSONObject

interface WebRtcViewerService {

    fun startViewing(broadcastId: String)

    fun stopViewing()

    fun handleOffer(broadcastId: String, json: JSONObject)
}