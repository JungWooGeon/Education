package com.pass.data.manager.socket

import io.socket.client.Socket
import org.json.JSONObject

interface SocketConnectionManager {

    fun initializeSocket(
        isBroadCaster: Boolean,
        handleRemoteIceCandidate: (JSONObject) -> Unit,
        handleError: () -> Unit,
        handleAnswer: (JSONObject) -> Unit,
        handleOffer: (JSONObject) -> Unit
    )

    fun connect(onEventConnect: () -> Unit)

    fun disconnect()

    fun getSocket(): Socket?
}