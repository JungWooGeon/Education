package com.pass.data.manager.capture

import org.webrtc.VideoTrack

interface VideoCaptureManager {
    fun startVideoCapture(): VideoTrack
    fun stopVideoCapture()
}