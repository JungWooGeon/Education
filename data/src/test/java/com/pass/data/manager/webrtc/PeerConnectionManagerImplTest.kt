package com.pass.data.manager.webrtc

import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test
import org.webrtc.AudioTrack
import org.webrtc.DataChannel
import org.webrtc.IceCandidate
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.RtpSender
import org.webrtc.RtpTransceiver
import org.webrtc.VideoTrack

class PeerConnectionManagerImplTest {

    private val mockPeerConnectionFactory = mockk<PeerConnectionFactory>()
    private val mockPeerConnection = mockk<PeerConnection>()
    private val mockRtcTransceiver = mockk<RtpTransceiver>()
    private val mockVideoTrack = mockk<VideoTrack>()
    private val mockAudioTrack = mockk<AudioTrack>()
    private val mockMediaStream = mockk<MediaStream>()
    private val mockDataChannel = mockk<DataChannel>()
    private val mockRtcSender = mockk<RtpSender>()

    private val peerConnectionManagerImpl = PeerConnectionManagerImpl(mockPeerConnectionFactory)

    @Test
    fun testCreatePeerConnection() {
        var isSuccessOnIceCandidate = false
        var isSuccessOnReceiveVideoTrack = false
        var isSuccessOnFailure = false

        val slot = slot<PeerConnection.Observer>()
        every { mockPeerConnectionFactory.createPeerConnection(any<PeerConnection.RTCConfiguration>(), capture(slot)) } answers {
            slot.captured.onAddStream(mockMediaStream)
            slot.captured.onIceConnectionChange(PeerConnection.IceConnectionState.CONNECTED)
            slot.captured.onIceConnectionReceivingChange(true)
            slot.captured.onIceGatheringChange(PeerConnection.IceGatheringState.GATHERING)
            slot.captured.onIceCandidatesRemoved(emptyArray())
            slot.captured.onRemoveStream(mockMediaStream)
            slot.captured.onDataChannel(mockDataChannel)
            slot.captured.onRenegotiationNeeded()
            mockPeerConnection
        }
        every { mockVideoTrack.kind() } returns "video"
        every { mockRtcTransceiver.receiver?.track() } returns mockVideoTrack
        every { mockVideoTrack.setEnabled(true) } returns true

        peerConnectionManagerImpl.createPeerConnection(
            callbackOnIceCandidate = { isSuccessOnIceCandidate = true },
            callbackOnReceiveVideoTrack = { isSuccessOnReceiveVideoTrack = true },
            callbackOnFailure = { isSuccessOnFailure = true }
        )

        slot.captured.onIceCandidate(IceCandidate("spdMid", 1, "sdp"))
        assertTrue(isSuccessOnIceCandidate)

        slot.captured.onTrack(mockRtcTransceiver)
        assertTrue(isSuccessOnReceiveVideoTrack)

        slot.captured.onSignalingChange(PeerConnection.SignalingState.CLOSED)
        assertTrue(isSuccessOnFailure)

        assertEquals(peerConnectionManagerImpl.getPeerConnection(), mockPeerConnection)
    }

    @Test
    fun testSuccessAddTrackPeerConnection() {
        every { mockPeerConnection.addTrack(any()) } returns mockRtcSender

        testCreatePeerConnection()
        peerConnectionManagerImpl.addTrackPeerConnection(mockVideoTrack, mockAudioTrack)

        verify { mockPeerConnection.addTrack(any()) }
    }

    @Test
    fun testSuccessDisposePeerConnection() {
        every { mockPeerConnection.dispose() } just runs

        testCreatePeerConnection()
        peerConnectionManagerImpl.disposePeerConnection {}

        verify { mockPeerConnection.dispose() }
    }

    @Test
    fun testFailDisposePeerConnection() {
        var isSuccessDispose = true
        val testException = Exception("Test Exception")
        every { mockPeerConnection.dispose() } throws testException

        testCreatePeerConnection()
        peerConnectionManagerImpl.disposePeerConnection {
            isSuccessDispose = false
        }

        assertFalse(isSuccessDispose)
    }
}