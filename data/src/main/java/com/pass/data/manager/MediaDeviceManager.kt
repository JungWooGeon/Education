package com.pass.data.manager

import android.content.Context
import org.webrtc.AudioTrack
import org.webrtc.Camera2Enumerator
import org.webrtc.EglBase
import org.webrtc.MediaConstraints
import org.webrtc.PeerConnectionFactory
import org.webrtc.SurfaceTextureHelper
import org.webrtc.VideoCapturer
import org.webrtc.VideoTrack
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaDeviceManager @Inject constructor(
    private val factory: PeerConnectionFactory,
    private val context: Context,
    private val eglBaseContext: EglBase.Context
) {

    private var videoCapturer: VideoCapturer? = null

    /**
     * Start videoCapture and Callback created VideoTrack and AudioTrack
     */
    fun startVideoCapture(callback: (VideoTrack, AudioTrack) -> Unit) {
        videoCapturer = createVideoCapturer()

        val videoSource = factory.createVideoSource(videoCapturer!!.isScreencast)

        // VideoCapturer 초기화 및 캡처
        videoCapturer?.initialize(SurfaceTextureHelper.create("CaptureThread", eglBaseContext), context, videoSource.capturerObserver)
        videoCapturer?.startCapture(720, 480, 30)

        // videoTrack 생성
        val videoTrack = factory.createVideoTrack("Video${UUID.randomUUID()}", videoSource)

        val audioSource = factory.createAudioSource(MediaConstraints())
        val audioTrack = factory.createAudioTrack("Audio${UUID.randomUUID()}", audioSource)

        videoTrack.setEnabled(true)
        audioTrack.setEnabled(true)

        callback(videoTrack, audioTrack)
    }

    /**
     * Video capture release
     */
    fun stopVideoCapture() {
        videoCapturer?.stopCapture()
        videoCapturer = null
    }

    /**
     * Create VideoCapturer with Camera2 API (Front Camera)
     */
    private fun createVideoCapturer(): VideoCapturer {
        val enumerator = Camera2Enumerator(context)
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