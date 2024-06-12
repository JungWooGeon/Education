package com.pass.data.manager.capture

import org.webrtc.AudioTrack
import org.webrtc.MediaConstraints
import org.webrtc.PeerConnectionFactory
import java.util.UUID
import javax.inject.Inject

class AudioCaptureManagerImpl @Inject constructor(
    private val peerConnectionFactory: PeerConnectionFactory,
): AudioCaptureManager {

    override fun startAudioCapture(): AudioTrack {
        val audioSource = peerConnectionFactory.createAudioSource(MediaConstraints())
        val audioTrack = peerConnectionFactory.createAudioTrack("Audio${UUID.randomUUID()}", audioSource)
        audioTrack.setEnabled(true)

        return audioTrack
    }
}