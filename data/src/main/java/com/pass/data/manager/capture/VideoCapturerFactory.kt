package com.pass.data.manager.capture

import org.webrtc.Camera2Enumerator
import org.webrtc.VideoCapturer
import javax.inject.Inject

class VideoCapturerFactory @Inject constructor(private val enumerator: Camera2Enumerator) {

    fun createVideoCapturer(): VideoCapturer {
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