package com.pass.data.manager.capture

import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Test
import org.webrtc.AudioSource
import org.webrtc.AudioTrack
import org.webrtc.PeerConnectionFactory

class AudioCaptureManagerImplTest {

    private val mockPeerConnectionFactory = mockk<PeerConnectionFactory>()
    private val mockAudioSource = mockk<AudioSource>()
    private val mockAudioTrack = mockk<AudioTrack>()

    private val audioCaptureManagerImpl = AudioCaptureManagerImpl(mockPeerConnectionFactory)

    @Before
    fun setup() {
        every { mockPeerConnectionFactory.createAudioSource(any()) } returns mockAudioSource
        every { mockPeerConnectionFactory.createAudioTrack(any(), any()) } returns mockAudioTrack
        every { mockAudioTrack.setEnabled(any()) } returns true
    }

    @Test
    fun testSuccessStartAudioCapture() {
        val audioTrack = audioCaptureManagerImpl.startAudioCapture()

        assertEquals(audioTrack, mockAudioTrack)
    }
}