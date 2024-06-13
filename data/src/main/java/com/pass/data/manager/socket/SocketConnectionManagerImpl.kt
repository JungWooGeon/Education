package com.pass.data.manager.socket

import com.pass.data.BuildConfig
import io.socket.client.IO
import io.socket.client.Socket
import okhttp3.OkHttpClient
import org.json.JSONObject
import javax.inject.Inject

class SocketConnectionManagerImpl @Inject constructor(
    private val okHttpClient: OkHttpClient
) : SocketConnectionManager {

    private var socket: Socket? = null

    override fun initializeSocket(
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

    override fun connect(onEventConnect: () -> Unit) {
        socket?.connect()
        socket?.on(Socket.EVENT_CONNECT) {
            onEventConnect()
        }
    }

    override fun disconnect() {
        socket?.disconnect()
    }

    override fun getSocket(): Socket? = socket
}