package com.pass.data.manager.webrtc

import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import junit.framework.TestCase.assertTrue
import org.junit.Before
import org.junit.Test
import org.webrtc.PeerConnection
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription

class SdpManagerImplTest {

    private val mockPeerConnectionManager = mockk<PeerConnectionManager>()
    private val mockSessionDescription = mockk<SessionDescription>()
    private val mockPeerConnection = mockk<PeerConnection>()

    private val sdpManagerImpl = SdpManagerImpl(mockPeerConnectionManager)

    @Before
    fun setup() {
        every { mockPeerConnectionManager.getPeerConnection() } returns mockPeerConnection
        every { mockPeerConnection.setLocalDescription(any(), any()) } just runs
        every { mockPeerConnection.localDescription } returns mockSessionDescription
    }

    @Test
    fun testCreateOfferPeerConnection() {
        var isSuccessOnSetSuccess = false
        var isFailure = false

        val slot = slot<SdpObserver>()
        every { mockPeerConnection.createOffer(capture(slot), any()) } answers {
            slot.captured.onCreateSuccess(mockSessionDescription)
            slot.captured.onSetSuccess()
            slot.captured.onCreateFailure("testFail")
            slot.captured.onSetFailure("testFail")
        }

        sdpManagerImpl.createOfferPeerConnection(
            callbackOnSetSuccess = { isSuccessOnSetSuccess = true },
            callbackOnFailure = { isFailure = true }
        )

        assertTrue(isSuccessOnSetSuccess)
        assertTrue(isFailure)
    }

    @Test
    fun testCreateAnswerPeerConnection() {
        var isSuccessOnSetSuccess = false
        var isFailure = false

        val slot = slot<SdpObserver>()
        every { mockPeerConnection.createAnswer(capture(slot), any()) } answers {
            slot.captured.onCreateSuccess(mockSessionDescription)
            slot.captured.onSetSuccess()
            slot.captured.onCreateFailure("testFail")
            slot.captured.onSetFailure("testFail")
        }

        sdpManagerImpl.createAnswerPeerConnection(
            callbackOnSetSuccess = { isSuccessOnSetSuccess = true },
            callbackOnFailure = { isFailure = true }
        )

        assertTrue(isSuccessOnSetSuccess)
        assertTrue(isFailure)
    }
}