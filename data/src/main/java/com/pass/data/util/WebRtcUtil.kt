package com.pass.data.util

import android.content.Context
import com.pass.data.BuildConfig
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import org.json.JSONObject
import org.webrtc.Camera2Enumerator
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
import org.webrtc.VideoSource
import org.webrtc.VideoTrack
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebRtcUtil @Inject constructor(private val context: Context) {

    private lateinit var peerConnectionFactory: PeerConnectionFactory
    private lateinit var localVideoTrack: VideoTrack
    private lateinit var remoteVideoTrack: VideoTrack
    private lateinit var localVideoSource: VideoSource
    private lateinit var peerConnection: PeerConnection

    private val socket: Socket = IO.socket(BuildConfig.SignalingServer)
    var onRemoteVideoTrackAvailable: ((VideoTrack) -> Unit)? = null

    private val iceServers = listOf(
        PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer()
    )

    private val onSdpAnswer = Emitter.Listener { args ->
        val data = args[0] as JSONObject
        val sdp = data.getString("sdp")
        val answer = SessionDescription(SessionDescription.Type.ANSWER, sdp)
        peerConnection.setRemoteDescription(sdpObserver, answer)
    }

    private val onIceCandidate = Emitter.Listener { args ->
        val data = args[0] as JSONObject
        val candidate = data.getString("candidate")
        val sdpMid = data.getString("sdpMid")
        val sdpMLineIndex = data.getInt("sdpMLineIndex")
        val iceCandidate = IceCandidate(sdpMid, sdpMLineIndex, candidate)
        peerConnection.addIceCandidate(iceCandidate)
    }

    init {
        socket.on("sdpAnswer", onSdpAnswer)
        socket.on("iceCandidate", onIceCandidate)
    }

    private val sdpObserver = object : SdpObserver {
        override fun onCreateSuccess(sessionDescription: SessionDescription) {
            peerConnection.setLocalDescription(this, sessionDescription)
            val sdp = JSONObject().apply {
                put("sdp", sessionDescription.description)
            }
            socket.emit("offer", sdp)
        }

        override fun onSetSuccess() {}
        override fun onCreateFailure(error: String) {}
        override fun onSetFailure(error: String) {}
    }

    private val peerConnectionObserver = object : PeerConnection.Observer {
        override fun onSignalingChange(newState: PeerConnection.SignalingState) {}
        override fun onIceConnectionChange(newState: PeerConnection.IceConnectionState) {}
        override fun onIceConnectionReceivingChange(receiving: Boolean) {}
        override fun onIceGatheringChange(newState: PeerConnection.IceGatheringState) {}
        override fun onIceCandidate(candidate: IceCandidate) {
            val candidateJson = JSONObject().apply {
                put("candidate", candidate.sdp)
                put("sdpMid", candidate.sdpMid)
                put("sdpMLineIndex", candidate.sdpMLineIndex)
            }
            socket.emit("iceCandidate", candidateJson)
        }

        override fun onIceCandidatesRemoved(p0: Array<out IceCandidate>?) {}
        override fun onAddStream(stream: MediaStream) {
            if (stream.videoTracks.isNotEmpty()) {
                remoteVideoTrack = stream.videoTracks[0]

                // UI에 원격 비디오 트랙을 표시하기 위해 데이터 저장
                onRemoteVideoTrackAvailable?.invoke(remoteVideoTrack)
            }
        }

        override fun onRemoveStream(stream: MediaStream) {}
        override fun onDataChannel(channel: DataChannel) {}
        override fun onRenegotiationNeeded() {}
        override fun onAddTrack(receiver: RtpReceiver, mediaStreams: Array<MediaStream>) {}
    }

    fun initializePeerConnectionFactory() {
        val options = PeerConnectionFactory.InitializationOptions.builder(context).createInitializationOptions()
        PeerConnectionFactory.initialize(options)

        val encoderFactory = DefaultVideoEncoderFactory(
            EglBase.create().eglBaseContext, true, true
        )
        val decoderFactory = DefaultVideoDecoderFactory(EglBase.create().eglBaseContext)

        peerConnectionFactory = PeerConnectionFactory.builder()
            .setVideoEncoderFactory(encoderFactory)
            .setVideoDecoderFactory(decoderFactory)
            .createPeerConnectionFactory()
    }

    fun createLocalMediaStream() {
        val videoCapturer = createVideoCapturer()
        localVideoSource = peerConnectionFactory.createVideoSource(videoCapturer.isScreencast)
        val surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", EglBase.create().eglBaseContext)
        videoCapturer.initialize(surfaceTextureHelper, context, localVideoSource.capturerObserver)
        videoCapturer.startCapture(1280, 720, 30)

        localVideoTrack = peerConnectionFactory.createVideoTrack("100", localVideoSource)

        val mediaStream = peerConnectionFactory.createLocalMediaStream("mediaStream")
        mediaStream.addTrack(localVideoTrack)
        peerConnection.addStream(mediaStream)
    }

    private fun createVideoCapturer(): VideoCapturer {
        val videoCapturer: VideoCapturer? = Camera2Enumerator(context).run {
            deviceNames.find { isFrontFacing(it) }?.let { createCapturer(it, null) }
        }
        return videoCapturer ?: throw IllegalStateException("No suitable video capturer found.")
    }

    fun createPeerConnection() {
        val rtcConfig = PeerConnection.RTCConfiguration(iceServers)
        peerConnection = peerConnectionFactory.createPeerConnection(rtcConfig, peerConnectionObserver)
            ?: throw IllegalStateException("Failed to create PeerConnection")

        peerConnection.createOffer(sdpObserver, MediaConstraints())
    }

    fun startBroadcast(broadcastId: String) {
        socket.connect()
        socket.emit("start", broadcastId)
    }

    fun startViewing(broadcastId: String) {
        socket.connect()
        socket.emit("join", broadcastId)
    }

    fun stopLiveStreaming() {
        // release
        peerConnection.close()
        peerConnection.dispose()

        // 이미 dispose 될 경우 catch
        try {
            localVideoTrack.dispose()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        localVideoSource.dispose()
        peerConnectionFactory.dispose()
    }

    fun stopViewing() {
        // 시청자는 자신이 받은 remoteVideoTrack을 해제
        remoteVideoTrack.dispose()

        // PeerConnection 종료 및 해제
        peerConnection.close()
        peerConnection.dispose()

        // PeerConnectionFactory 해제
        peerConnectionFactory.dispose()
    }
}