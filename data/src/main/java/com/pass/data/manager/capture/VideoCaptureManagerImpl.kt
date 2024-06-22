package com.pass.data.manager.capture

import android.content.Context
import org.webrtc.EglBase
import org.webrtc.PeerConnectionFactory
import org.webrtc.SurfaceTextureHelper
import org.webrtc.VideoCapturer
import org.webrtc.VideoTrack
import java.util.UUID
import javax.inject.Inject

class VideoCaptureManagerImpl @Inject constructor(
    private val peerConnectionFactory: PeerConnectionFactory,
    private val videoCapturerFactory: VideoCapturerFactory,
    private val eglBaseContext: EglBase.Context,
    private val context: Context
): VideoCaptureManager {

    private var videoCapturer: VideoCapturer? = null

    /**
     * Start videoCapture and Callback created VideoTrack and AudioTrack
     */
    override fun startVideoCapture(): VideoTrack {
        videoCapturer = videoCapturerFactory.createVideoCapturer()

        val videoSource = peerConnectionFactory.createVideoSource(videoCapturer!!.isScreencast)

        // VideoCaptureManager 초기화 및 캡처
        videoCapturer?.initialize(SurfaceTextureHelper.create("CaptureThread", eglBaseContext), context, videoSource.capturerObserver)
        videoCapturer?.startCapture(720, 480, 30)

        // videoTrack 생성
        val videoTrack = peerConnectionFactory.createVideoTrack("Video${UUID.randomUUID()}", videoSource)
        videoTrack.setEnabled(true)

        return videoTrack
    }


    /**
     * Video capture release
     */
    override fun stopVideoCapture() {
        videoCapturer?.stopCapture()
    }
}