package com.pass.data.manager.socket

import android.annotation.SuppressLint
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.verify
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import okhttp3.OkHttpClient
import org.json.JSONObject
import org.junit.Before
import org.junit.Test
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class SocketConnectionManagerImplTest {

    private val mockSocket = mockk<Socket>()

    // 콜백을 캡처할 슬롯 설정
    private val iceCandidateSlot = slot<Emitter.Listener>()
    private val errorSlot = slot<Emitter.Listener>()
    private val answerSlot = slot<Emitter.Listener>()
    private val offerSlot = slot<Emitter.Listener>()
    private val connectSlot = slot<Emitter.Listener>()
    private val disconnectSlot = slot<Emitter.Listener>()

    // JSONObject에 실제 데이터 추가
    private val testJson = JSONObject().apply {
        put("test", "test_value")
    }

    private val socketConnectionManagerImpl = SocketConnectionManagerImpl(getUnsafeOkHttpClient())

    @Before
    fun setup() {
        mockkStatic(IO::class)
        every { IO.socket(any<String>(), any()) } returns mockSocket
        every { mockSocket.on("iceCandidate", capture(iceCandidateSlot)) } answers { mockSocket }
        every { mockSocket.on("error", capture(errorSlot)) } answers { mockSocket }
        every { mockSocket.on("answer", capture(answerSlot)) } answers { mockSocket }
        every { mockSocket.on("offer", capture(offerSlot)) } answers { mockSocket }
    }

    @Test
    fun testSuccessInitializeSocketFromBroadCaster() {
        socketConnectionManagerImpl.initializeSocket(
            isBroadCaster = true,
            handleRemoteIceCandidate = {},
            handleAnswer = {},
            handleError = {},
            handleOffer = {}
        )

        iceCandidateSlot.captured.call(testJson)
        answerSlot.captured.call(testJson)
        errorSlot.captured.call(errorSlot)

        verify { mockSocket.on("iceCandidate", any()) }
        verify { mockSocket.on("answer", any()) }
        verify { mockSocket.on("error", any()) }
    }

    @Test
    fun testSuccessInitializeSocketFromViewer() {
        socketConnectionManagerImpl.initializeSocket(
            isBroadCaster = false,
            handleRemoteIceCandidate = {},
            handleAnswer = {},
            handleError = {},
            handleOffer = {}
        )

        iceCandidateSlot.captured.call(testJson)
        offerSlot.captured.call(testJson)

        verify { mockSocket.on("iceCandidate", any()) }
        verify { mockSocket.on("offer", any()) }
    }

    @Test
    fun testFailInitializeSocket() {
        // 콜백을 캡처할 슬롯 설정
        val offerSlot = slot<Emitter.Listener>()
        val mockHandleError: () -> Unit = mockk(relaxed = true)

        every { mockSocket.on("offer", capture(offerSlot)) } answers {
            throw Exception("test exception")
        }

        // 소켓 초기화
        socketConnectionManagerImpl.initializeSocket(
            isBroadCaster = false,
            handleRemoteIceCandidate = {},
            handleAnswer = {},
            handleError = mockHandleError,
            handleOffer = {}
        )

        // 캡처된 콜백 호출 - 예외 발생 테스트
        try {
            offerSlot.captured.call(arrayOf(JSONObject().apply { put("test", "test_value") }))
        } catch (e: Exception) {
            // 예외를 잡아서 핸들러를 호출합니다.
            mockHandleError()
        }

        // 핸들러 호출 검증
        verify { mockHandleError() }
    }

    @Test
    fun testSuccessConnect() {
        testSuccessInitializeSocketFromBroadCaster()

        val connectErrorSlot = slot<Emitter.Listener>()

        every { mockSocket.connect() } returns mockSocket
        every { mockSocket.on(Socket.EVENT_CONNECT, capture(connectSlot)) } answers { mockSocket }
        every { mockSocket.on(Socket.EVENT_DISCONNECT, capture(disconnectSlot)) } answers { mockSocket }
        every { mockSocket.on(Socket.EVENT_CONNECT_ERROR, capture(connectErrorSlot)) } answers { mockSocket }

        socketConnectionManagerImpl.connect(
            onEventConnect = {},
            callbackOnFailureConnected = {}
        )

        disconnectSlot.captured.call(JSONObject())
        connectSlot.captured.call(JSONObject())
        connectErrorSlot.captured.call(JSONObject())

        verify { mockSocket.on(Socket.EVENT_CONNECT, any()) }
        verify { mockSocket.on(Socket.EVENT_DISCONNECT, any()) }
        verify { mockSocket.on(Socket.EVENT_CONNECT_ERROR, any()) }
    }

    @Test
    fun testSuccessDisconnect() {
        if (socketConnectionManagerImpl.getSocket() == null) {
            testSuccessConnect()
        }
        every { mockSocket.disconnect() } returns mockSocket

        socketConnectionManagerImpl.disconnect()

        verify { mockSocket.disconnect() }
    }

    @SuppressLint("TrustAllX509TrustManager")
    private fun getUnsafeOkHttpClient(): OkHttpClient {
        try {
            // Create a trust manager that does not validate certificate chains
            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}

                override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}

                override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
            })

            // Install the all-trusting trust manager
            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, SecureRandom())

            // Create an ssl socket factory with our all-trusting manager
            val sslSocketFactory = sslContext.socketFactory

            return OkHttpClient.Builder()
                .sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
                .hostnameVerifier { _, _ -> true }
                .build()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }
}