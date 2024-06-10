package com.pass.data.util

import android.content.Context
import android.util.Log
import com.pass.data.BuildConfig
import io.socket.client.IO
import io.socket.client.Socket
import okhttp3.OkHttpClient
import org.json.JSONObject
import org.webrtc.AddIceObserver
import org.webrtc.Camera2Enumerator
import org.webrtc.CameraEnumerator
import org.webrtc.DataChannel
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.EglBase
import org.webrtc.IceCandidate
import org.webrtc.MediaConstraints
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.RtpTransceiver
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import org.webrtc.SurfaceTextureHelper
import org.webrtc.VideoCapturer
import org.webrtc.VideoDecoderFactory
import org.webrtc.VideoEncoderFactory
import org.webrtc.VideoTrack
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

@Singleton
class WebRtcUtil @Inject constructor(
    private val context: Context,
    private val eglBaseContext: EglBase.Context
) {
    private var peerConnection: PeerConnection? = null
    private lateinit var socket: Socket
    lateinit var broadcastId: String
    var onRemoteVideoTrackAvailable: ((VideoTrack) -> Unit)? = null
    var onConnectionFailed: (() -> Unit)? = null
    var onSuccessBroadCast: ((VideoTrack) -> Unit)? = null
    private var videoCapturer: VideoCapturer? = null
    private lateinit var videoTrack: VideoTrack
    private lateinit var viewerVideoTrack: VideoTrack

    private val iceCandidateList = mutableListOf<JSONObject>()
    private var isConnected = false

    private val factory by lazy {
        PeerConnectionFactory.initialize(
            PeerConnectionFactory.InitializationOptions.builder(context)
                .setEnableInternalTracer(true)
                .createInitializationOptions()
        )
        val videoDecoderFactory: VideoDecoderFactory = DefaultVideoDecoderFactory(eglBaseContext)
        val videoEncoderFactory: VideoEncoderFactory = DefaultVideoEncoderFactory(eglBaseContext, true, true)

        PeerConnectionFactory.builder()
            .setVideoDecoderFactory(videoDecoderFactory)
            .setVideoEncoderFactory(videoEncoderFactory)
            .createPeerConnectionFactory()
    }

    init {
        initializeSocket()
        initializePeerConnectionFactory()
    }

    private fun initializePeerConnectionFactory() {
        val options = PeerConnectionFactory.InitializationOptions.builder(context)
            .createInitializationOptions()
        PeerConnectionFactory.initialize(options)
    }

    private fun initializeSocket() {
        try {
            val okHttpClient = getUnsafeOkHttpClient()
            val opts = IO.Options()
            opts.callFactory = okHttpClient
            opts.webSocketFactory = okHttpClient

            socket = IO.socket(BuildConfig.SignalingServer, opts)
            socket.on(Socket.EVENT_CONNECT) {
                Log.d("WebRtcUtil", "Connected to signaling server")
            }.on(Socket.EVENT_CONNECT_ERROR) { args ->
                if (args.isNotEmpty() && args[0] is Exception) {
                    val e = args[0] as Exception
                    Log.e("WebRtcUtil", "Connection failed: ${e.message}")
                } else {
                    Log.e("WebRtcUtil", "Connection failed: ${args.joinToString()}")
                }
            }.on("iceCandidate") { args ->
                handleRemoteIceCandidate(args[0] as JSONObject)
            }.on("offer") { args ->
                handleOffer(args[0] as JSONObject)
            }.on("answer") { args ->
                handleAnswer(args[0] as JSONObject)
            }.on("error") {
                handleError()
            }
        } catch (e: Exception) {
            Log.e("WebRtcUtil", "Socket initialization error: ${e.message}")
        }
    }

    fun startBroadcast(broadcastId: String) {
        this.broadcastId = broadcastId

        createPeerConnection()
        // setup and start camera capture (with create VideoTrack)
        setupMediaDevices()

        // 연결 시도 후 offer 생성
        socket.connect()
        socket.on(Socket.EVENT_CONNECT) {
            createOffer(broadcastId)
        }
    }

    fun stopBroadcast() {
        // video capture release
        videoCapturer?.stopCapture()
        videoCapturer = null

        try {
            peerConnection?.dispose()
        } catch(e: Exception) {
            e.printStackTrace()
        }

        socket.emit("stop", broadcastId)
        socket.disconnect()
    }

    fun startViewing(broadcastId: String) {
        this.broadcastId = broadcastId

        isConnected = true
        createPeerConnection()

        // 연결 시도
        socket.connect()
        socket.emit("join", broadcastId)
    }

    fun stopViewing() {
        try {
            peerConnection?.dispose()
        } catch(e: Exception) {
            e.printStackTrace()
        }

//        socket.emit("disconnect", broadcastId)
        socket.disconnect()
    }

    private fun createPeerConnection() {
        val iceServers = listOf(
            PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer(),
            PeerConnection.IceServer.builder(BuildConfig.turnServer).setUsername(BuildConfig.turnServerUserName).setPassword(BuildConfig.turnServerUserPassword).createIceServer()
        )
        val rtcConfig = PeerConnection.RTCConfiguration(iceServers).apply {
            sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
        }

        peerConnection = factory.createPeerConnection(rtcConfig, object : PeerConnection.Observer {
            override fun onIceCandidate(candidate: IceCandidate) {
                val json = JSONObject().put("candidate", candidate.sdp).put("sdpMid", candidate.sdpMid).put("sdpMLineIndex", candidate.sdpMLineIndex)
                println("ICE 전송")
                println(json)

                if (isConnected) {
                    socket.emit("iceCandidate", broadcastId, json)
                } else {
                    iceCandidateList.add(json)
                }
            }

            override fun onTrack(transceiver: RtpTransceiver?) {
                super.onTrack(transceiver)

                Log.e("onTrack", "이건 다른 함수네?")
                println(transceiver?.receiver?.track())

                if (transceiver?.receiver?.track()?.kind() == "video") {
                    println("이것은 비디오 트랙!")
                    viewerVideoTrack = (transceiver.receiver?.track() as VideoTrack)
                    viewerVideoTrack.setEnabled(true)
                    onSuccessBroadCast?.invoke(videoTrack)
                } else {
                    println("이것은 오디오 트랙!")
                }
            }

            override fun onAddStream(stream: MediaStream) {
                Log.d("스트림 테스트", "스트림 도착 ${stream.videoTracks.size}개의 트랙")
                Log.d("스트림 테스트", "${stream.videoTracks}")
            }
            override fun onIceConnectionChange(newState: PeerConnection.IceConnectionState?) {
                Log.d("WebRTC", "ICE Connection State: " + newState.toString())

                if (newState == PeerConnection.IceConnectionState.CONNECTED) {
                    peerConnection?.getStats { statsReport ->
                        Log.e("여기 확인", "여기!!!")
                        println(statsReport)
                    }
                    onRemoteVideoTrackAvailable?.invoke(viewerVideoTrack)
                }
            }
            override fun onSignalingChange(state: PeerConnection.SignalingState?) {
                Log.d("WebRTC", "Signaling State: " + state.toString())
            }
            override fun onIceConnectionReceivingChange(p0: Boolean) {}
            override fun onIceGatheringChange(state: PeerConnection.IceGatheringState?) {
                when (state) {
                    PeerConnection.IceGatheringState.NEW -> Log.d("ICE CHANGE", "NEW")
                    PeerConnection.IceGatheringState.GATHERING -> Log.d("ICE CHANGE", "GATHERING")
                    PeerConnection.IceGatheringState.COMPLETE -> Log.d("ICE CHANGE", "COMPLETE")
                    null -> Log.d("ICE CHANGE", "NULL")
                }
            }
            override fun onIceCandidatesRemoved(p0: Array<out IceCandidate>?) {}
            override fun onRemoveStream(p0: MediaStream?) {}
            override fun onDataChannel(p0: DataChannel?) {}
            override fun onRenegotiationNeeded() {}
        })
    }

    private fun createOffer(broadcastId: String) {
        peerConnection?.createOffer(object : SdpObserver {
            override fun onCreateSuccess(sessionDescription: SessionDescription) {
                println("SDP 생성 완료, 로컬 설정 시작 ${sessionDescription.description}")
                parseSdpForCodecInfo(sessionDescription)
                peerConnection?.setLocalDescription(this, sessionDescription)
            }

            override fun onSetSuccess() {
                println("start 신호 보내기 전")
                socket.emit("start", broadcastId, peerConnection?.localDescription?.description)
                println("start 신호 보내기 완료")
            }

            override fun onCreateFailure(p0: String?) {
                println("SDP 생성 실패, $p0")
            }

            override fun onSetFailure(p0: String?) {
                Log.e("onSetFailure 오류", p0.toString())
            }
        }, MediaConstraints())
    }

    private fun parseSdpForCodecInfo(sdp: SessionDescription) {
        val lines = sdp.description.split("\n")
        val codecInfo = lines.filter { it.startsWith("a=rtpmap:") }
        codecInfo.forEach { Log.d("WebRtcUtil", "SDP 코덱 정보: $it") }
    }

    private fun createAnswer(broadcastId: String) {
        peerConnection?.createAnswer(object : SdpObserver {
            override fun onCreateSuccess(sessionDescription: SessionDescription) {
                println("answer 요청 직전")
                peerConnection?.setLocalDescription(this, sessionDescription)
            }

            override fun onCreateFailure(p0: String?) {
                println("createAnswer 실패 $p0")
            }
            override fun onSetSuccess() {
                socket.emit("answer", broadcastId, peerConnection?.localDescription?.description)
                println("answer 요청 완료")
            }
            override fun onSetFailure(p0: String?) {}
        }, MediaConstraints())
    }

    private fun handleRemoteIceCandidate(json: JSONObject) {
        val candidate = IceCandidate(
            json.getString("sdpMid"),
            json.getInt("sdpMLineIndex"),
            json.getString("candidate")
        )

        println("ICE Candidate 받아옴")
        println(candidate)
        peerConnection?.addIceCandidate(candidate, object : AddIceObserver {
            override fun onAddSuccess() {
                println("ADD ICE Candidate 성공")
            }

            override fun onAddFailure(p0: String?) {
                println("ADD ICE Candidate 실패")
            }
        })
    }

    private fun handleOffer(json: JSONObject) {
        println("offer 전달 받음")
        val offerSdp = SessionDescription(SessionDescription.Type.OFFER, json.getString("sdp"))

        println(offerSdp.description)
        if (offerSdp.description.contains("H264")) {
            println("H264 포함")
        }

        peerConnection?.setRemoteDescription(object : SdpObserver {
            override fun onSetSuccess() {
                println("answer 생성 직전")
                createAnswer(broadcastId)
            }

            override fun onCreateSuccess(p0: SessionDescription?) {}
            override fun onCreateFailure(p0: String?) {}
            override fun onSetFailure(p0: String?) {}
        }, offerSdp)
    }

    private fun handleAnswer(json: JSONObject) {
        println("answer 받음")

        val answerSdp = SessionDescription(SessionDescription.Type.ANSWER, json.getString("sdp"))
        peerConnection?.setRemoteDescription(object : SdpObserver {
            override fun onCreateSuccess(p0: SessionDescription?) {}

            override fun onSetSuccess() {
                println("answer 적용 성공")
//                onSuccessBroadCast?.invoke(videoTrack)
                peerConnection?.remoteDescription?.let { parseSdpForCodecInfo(it) }

                iceCandidateList.forEach {
                    socket.emit("iceCandidate", broadcastId, it)
                }
                iceCandidateList.clear()
                isConnected = true
            }
            override fun onCreateFailure(p0: String?) {
                println("answer 적용 실패 $p0")
            }
            override fun onSetFailure(p0: String?) {}
        }, answerSdp)
    }

    private fun handleError() {
        try {
            peerConnection?.dispose()
        } catch(e: Exception) {
            e.printStackTrace()
        }

        socket.disconnect()
        onConnectionFailed?.invoke()
    }

    private fun setupMediaDevices() {
        // Camera2 API를 사용한 비디오 캡처
        videoCapturer = createCameraCapturer(Camera2Enumerator(context))
        val videoSource = factory.createVideoSource(videoCapturer!!.isScreencast)
        videoCapturer?.initialize(SurfaceTextureHelper.create("CaptureThread", eglBaseContext), context, videoSource.capturerObserver)
        videoCapturer?.startCapture(720, 480, 30)

        val videoTrack = factory.createVideoTrack("Video${UUID.randomUUID()}", videoSource)

        val audioSource = factory.createAudioSource(MediaConstraints())
        val audioTrack = factory.createAudioTrack("Audio${UUID.randomUUID()}", audioSource)
        audioTrack.setEnabled(true)
        videoTrack.setEnabled(true)

        this.videoTrack = videoTrack

        peerConnection?.addTrack(this.videoTrack)
        peerConnection?.addTrack(audioTrack)
    }

    private fun createCameraCapturer(enumerator: CameraEnumerator): VideoCapturer {
        val deviceNames = enumerator.deviceNames
        for (deviceName in deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                val videoCapturer = enumerator.createCapturer(deviceName, null)
                if (videoCapturer != null) {
                    return videoCapturer
                }
            }
        }
        throw RuntimeException("Failed to open front facing camera")
    }

    fun getUnsafeOkHttpClient(): OkHttpClient {
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
