package com.pass.data.service.webrtc

import org.json.JSONObject

interface WebRtcBaseService {

    fun handleRemoteIceCandidate(json: JSONObject)

    fun handleError(callbackOnFailureConnected: (() -> Unit))
}