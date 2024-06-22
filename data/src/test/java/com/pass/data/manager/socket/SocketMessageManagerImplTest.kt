package com.pass.data.manager.socket

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.socket.emitter.Emitter
import org.json.JSONObject
import org.junit.Before
import org.junit.Test

class SocketMessageManagerImplTest {

    private val mockSocketConnectionManager = mockk<SocketConnectionManager>()
    private val mockEmitter = mockk<Emitter>()
    private val mockJSONObject = mockk<JSONObject>()

    private val socketMessageManagerImpl = SocketMessageManagerImpl(mockSocketConnectionManager)

    @Before
    fun setup() {
        every { mockSocketConnectionManager.getSocket()?.emit("start", "testId", "testSessionDescription") } returns mockEmitter
        every { mockSocketConnectionManager.getSocket()?.emit("stop", "testId") } returns mockEmitter
        every { mockSocketConnectionManager.getSocket()?.emit("join", "testId") } returns mockEmitter
        every { mockSocketConnectionManager.getSocket()?.emit("iceCandidate", "testId", mockJSONObject) } returns mockEmitter
        every { mockSocketConnectionManager.getSocket()?.emit("answer", "testId", "testSessionDescription") } returns mockEmitter
    }

    @Test
    fun testSuccessEmitMessage() {
        val testMessageList = listOf("start", "stop", "join", "iceCandidate", "answer")

        for(testMessage in testMessageList) {
            when(testMessage) {
                "start", "answer" -> {
                    socketMessageManagerImpl.emitMessage(
                        message = testMessage,
                        broadcastId = "testId",
                        sessionDescription = "testSessionDescription"
                    )

                    verify { mockSocketConnectionManager.getSocket()?.emit(testMessage, "testId", "testSessionDescription") }
                }
                "stop", "join" -> {
                    socketMessageManagerImpl.emitMessage(
                        message = testMessage,
                        broadcastId = "testId"
                    )
                    verify { mockSocketConnectionManager.getSocket()?.emit(testMessage, "testId") }
                }
                "iceCandidate" -> {
                    socketMessageManagerImpl.emitMessage(
                        message = testMessage,
                        broadcastId = "testId",
                        iceCandidate = mockJSONObject
                    )

                    verify { mockSocketConnectionManager.getSocket()?.emit(testMessage, "testId", mockJSONObject) }
                }
            }
        }
    }
}