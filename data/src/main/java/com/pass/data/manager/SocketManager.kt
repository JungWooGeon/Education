package com.pass.data.manager

import com.pass.data.BuildConfig
import io.socket.client.IO
import io.socket.client.Socket
import okhttp3.OkHttpClient
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SocketManager @Inject constructor(
    private val okHttpClient: OkHttpClient
) {

    private var socket: Socket? = null

    fun initializeSocket(
        isBroadCaster: Boolean,
        handleRemoteIceCandidate: (JSONObject) -> Unit,
        handleError: () -> Unit,
        handleAnswer: (JSONObject) -> Unit,
        handleOffer: (JSONObject) -> Unit
    ) {
        try {
            val opts = IO.Options()
            opts.callFactory = okHttpClient
            opts.webSocketFactory = okHttpClient

            socket = IO.socket(BuildConfig.SignalingServer, opts)
            socket?.on("iceCandidate") { args ->
                handleRemoteIceCandidate(args[0] as JSONObject)
            }?.on("error") {
                handleError()
            }

            if (isBroadCaster) {
                socket?.on("answer") { args ->
                    handleAnswer(args[0] as JSONObject)
                }
            } else {
                socket?.on("offer") { args ->
                    handleOffer(args[0] as JSONObject)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun connect(onEventConnect: () -> Unit) {
        socket?.connect()
        socket?.on(Socket.EVENT_CONNECT) {
            onEventConnect()
        }
    }

    fun disconnect() {
        socket?.disconnect()
    }

    fun emitMessage(
        message: String,
        broadcastId: String,
        sessionDescription: String? = null,
        iceCandidate: JSONObject? = null
    ) {
        when (message) {
            "start" -> socket?.emit(message, broadcastId, sessionDescription)
            "stop" -> socket?.emit(message, broadcastId)
            "join" -> socket?.emit(message, broadcastId)
            "iceCandidate" -> socket?.emit(message, broadcastId, iceCandidate)
            "answer" -> socket?.emit(message, broadcastId, sessionDescription)
        }
    }
}