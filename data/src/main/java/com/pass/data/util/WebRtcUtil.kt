package com.pass.data.util

import android.content.Context
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

class WebRtcUtil @Inject constructor(private val context: Context) {

    private lateinit var peerConnectionFactory: PeerConnectionFactory
    private lateinit var localVideoTrack: VideoTrack
    private lateinit var remoteVideoTrack: VideoTrack
    private lateinit var localVideoSource: VideoSource

    private val iceServers = listOf(
        PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer()
    )

    private val sdpObserver = object : SdpObserver {
        override fun onCreateSuccess(sessionDescription: SessionDescription) {
            peerConnection.setLocalDescription(this, sessionDescription)
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
            // 시그널링 서버를 통해 원격 피어에게 ICE 후보를 보냅니다.
        }

        override fun onIceCandidatesRemoved(p0: Array<out IceCandidate>?) {

        }

        override fun onAddStream(stream: MediaStream) {
            if (stream.videoTracks.isNotEmpty()) {
                remoteVideoTrack = stream.videoTracks[0]
                // 원격 비디오 트랙을 사용하여 UI에 표시합니다.
            }
        }

        override fun onRemoveStream(stream: MediaStream) {}
        override fun onDataChannel(channel: DataChannel) {}
        override fun onRenegotiationNeeded() {}
        override fun onAddTrack(receiver: RtpReceiver, mediaStreams: Array<MediaStream>) {}
    }

    private lateinit var peerConnection: PeerConnection

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
}
