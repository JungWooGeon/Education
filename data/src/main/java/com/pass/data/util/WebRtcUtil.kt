package com.pass.data.util

import android.content.Context
import android.util.Log
import com.pass.data.BuildConfig
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject
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
import org.webrtc.RtpReceiver
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import org.webrtc.SurfaceTextureHelper
import org.webrtc.VideoCapturer
import org.webrtc.VideoDecoderFactory
import org.webrtc.VideoEncoderFactory
import org.webrtc.VideoTrack
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class WebRtcUtil @Inject constructor(private val context: Context) {
    private var peerConnection: PeerConnection? = null
    private lateinit var socket: Socket
    lateinit var broadcastId: String
    var onRemoteVideoTrackAvailable: ((VideoTrack) -> Unit)? = null
    var onConnectionFailed: (() -> Unit)? = null
    var onSuccessBroadCast: (() -> Unit)? = null

    private val factory by lazy {
        PeerConnectionFactory.initialize(
            PeerConnectionFactory.InitializationOptions.builder(context)
                .setEnableInternalTracer(false)
                .createInitializationOptions()
        )
        val eglContext = EglBase.create().eglBaseContext
        val videoDecoderFactory: VideoDecoderFactory = DefaultVideoDecoderFactory(eglContext)
        val videoEncoderFactory: VideoEncoderFactory = DefaultVideoEncoderFactory(eglContext, true, true)

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
            socket = IO.socket(BuildConfig.SignalingServer)
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
        setupMediaDevices()

        // 연결 시도 후 offer 생성
        socket.connect()
        socket.on(Socket.EVENT_CONNECT) {
            createOffer(broadcastId)
        }
    }

    fun stopBroadcast() {
        try {
            peerConnection?.dispose()
        } catch(e: Exception) {
            e.printStackTrace()
        }

        socket.emit("stop", broadcastId)
        socket.emit("disconnect_request", broadcastId)
        socket.disconnect()
    }

    fun startViewing(broadcastId: String) {
        this.broadcastId = broadcastId

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

        socket.emit("disconnect_request", broadcastId)
        socket.disconnect()
    }

    private fun createPeerConnection() {
        val iceServers = listOf(PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer())
        val rtcConfig = PeerConnection.RTCConfiguration(iceServers).apply {
            sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
        }

        peerConnection = factory.createPeerConnection(rtcConfig, object : PeerConnection.Observer {
            override fun onIceCandidate(candidate: IceCandidate) {
                sendLocalIceCandidate(candidate)
            }

            override fun onAddTrack(rtpReceiver: RtpReceiver?, mediaStreams: Array<out MediaStream>?) {
                mediaStreams?.firstOrNull()?.videoTracks?.firstOrNull()?.let {
                    onRemoteVideoTrackAvailable?.invoke(it)
                }
            }

            override fun onAddStream(stream: MediaStream) {}
            override fun onIceConnectionChange(newState: PeerConnection.IceConnectionState?) {}
            override fun onSignalingChange(state: PeerConnection.SignalingState?) {}
            override fun onIceConnectionReceivingChange(p0: Boolean) {}
            override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState?) {}
            override fun onIceCandidatesRemoved(p0: Array<out IceCandidate>?) {}
            override fun onRemoveStream(p0: MediaStream?) {}
            override fun onDataChannel(p0: DataChannel?) {}
            override fun onRenegotiationNeeded() {}
        })
    }

    private fun createOffer(broadcastId: String) {
        peerConnection?.createOffer(object : SdpObserver {
            override fun onCreateSuccess(sessionDescription: SessionDescription) {
                val newDescription = SessionDescription(
                    sessionDescription.type,
                    sessionDescription.description.codec(),
                )

                println("SDP 생성 완료, 로컬 설정 시작 ${newDescription.description}")
                parseSdpForCodecInfo(newDescription)
                peerConnection?.setLocalDescription(this, newDescription)
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

    private fun String.codec(): String {
        return this.replace("vp9", "VP9").replace("vp8", "VP8").replace("h264", "H264")
    }

    private fun parseSdpForCodecInfo(sdp: SessionDescription) {
        val lines = sdp.description.split("\n")
        val codecInfo = lines.filter { it.startsWith("a=rtpmap:") }
        codecInfo.forEach { Log.d("WebRtcUtil", "SDP 코덱 정보: $it") }
    }

    private fun createAnswer(broadcastId: String) {
        peerConnection?.createAnswer(object : SdpObserver {
            override fun onCreateSuccess(sessionDescription: SessionDescription) {
                peerConnection?.setLocalDescription(this, sessionDescription)
                socket.emit("answer", broadcastId, sessionDescription.description)
            }

            override fun onCreateFailure(p0: String?) {
                println("실패 $p0")
            }
            override fun onSetSuccess() {}
            override fun onSetFailure(p0: String?) {}
        }, MediaConstraints())
    }

    private fun handleRemoteIceCandidate(json: JSONObject) {
        val candidate = IceCandidate(
            json.getString("sdpMid"),
            json.getInt("sdpMLineIndex"),
            json.getString("candidate")
        )
        peerConnection?.addIceCandidate(candidate)
    }

    private fun handleOffer(json: JSONObject) {
        println("offer 전달 받음")
        val offerSdp = SessionDescription(SessionDescription.Type.OFFER, json.getString("sdp"))
        peerConnection?.setRemoteDescription(object : SdpObserver {
            override fun onSetSuccess() {
                println("ansewr 생성 직전")
                createAnswer(json.getString("broadcastId"))
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
                onSuccessBroadCast?.invoke()
            }
            override fun onCreateFailure(p0: String?) {
                println("answer 적용 실패 $p0")
            }
            override fun onSetFailure(p0: String?) {}
        }, answerSdp)
    }

    private fun handleError() {
        socket.emit("stop", broadcastId)
        socket.emit("disconnect_request", broadcastId)

        try {
            peerConnection?.dispose()
        } catch(e: Exception) {
            e.printStackTrace()
        }

        socket.disconnect()
        onConnectionFailed?.invoke()
    }

    private fun sendLocalIceCandidate(candidate: IceCandidate) {
        val json = JSONObject().put("candidate", candidate.sdp).put("sdpMid", candidate.sdpMid).put("sdpMLineIndex", candidate.sdpMLineIndex)
        socket.emit("iceCandidate", json)
    }

    private fun setupMediaDevices() {
        val eglBaseContext = EglBase.create().eglBaseContext

        // Camera2 API를 사용한 비디오 캡처
        val videoCapturer = createCameraCapturer(Camera2Enumerator(context))
        val videoSource = factory.createVideoSource(videoCapturer.isScreencast)
        videoCapturer.initialize(SurfaceTextureHelper.create("CaptureThread", eglBaseContext), context, videoSource.capturerObserver)
        videoCapturer.startCapture(720, 480, 30)

        val videoTrack = factory.createVideoTrack("Video${UUID.randomUUID()}", videoSource)

        val audioSource = factory.createAudioSource(MediaConstraints())
        val audioTrack = factory.createAudioTrack("Audio${UUID.randomUUID()}", audioSource)

        peerConnection?.addTrack(videoTrack)
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
}
