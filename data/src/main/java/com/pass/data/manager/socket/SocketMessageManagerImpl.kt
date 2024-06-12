package com.pass.data.manager.socket

import org.json.JSONObject
import javax.inject.Inject

class SocketMessageManagerImpl @Inject constructor(
    private val socketConnectionManager: SocketConnectionManager
): SocketMessageManager {

    override fun emitMessage(
        message: String,
        broadcastId: String,
        sessionDescription: String?,
        iceCandidate: JSONObject?
    ) {
        when (message) {
            "start" -> socketConnectionManager.getSocket()?.emit(message, broadcastId, sessionDescription)
            "stop" -> socketConnectionManager.getSocket()?.emit(message, broadcastId)
            "join" -> socketConnectionManager.getSocket()?.emit(message, broadcastId)
            "iceCandidate" -> socketConnectionManager.getSocket()?.emit(message, broadcastId, iceCandidate)
            "answer" -> socketConnectionManager.getSocket()?.emit(message, broadcastId, sessionDescription)
        }
    }
}