package com.pass.data.manager.socket

import org.json.JSONObject

interface SocketMessageManager {

    fun emitMessage(
        message: String,
        broadcastId: String,
        sessionDescription: String? = null,
        iceCandidate: JSONObject? = null
    )
}