package com.pass.data.manager.capture

import org.webrtc.AudioTrack

interface AudioCaptureManager {
    fun startAudioCapture(): AudioTrack
}