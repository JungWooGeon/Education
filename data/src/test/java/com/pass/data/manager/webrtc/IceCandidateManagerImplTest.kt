package com.pass.data.manager.webrtc

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.json.JSONObject
import org.junit.Before
import org.junit.Test
import org.webrtc.AddIceObserver
import org.webrtc.PeerConnection
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription

class IceCandidateManagerImplTest {

    private val mockPeerConnectionManager = mockk<PeerConnectionManager>()
    private val mockPeerConnection = mockk<PeerConnection>(relaxed = true)

    private val iceCandidateManagerImpl = IceCandidateManagerImpl(mockPeerConnectionManager)

    @Before
    fun setup() {
        every { mockPeerConnectionManager.getPeerConnection() } returns mockPeerConnection
    }

    @Test
    fun testSuccessSetRemoteDescriptionPeerConnection() {
        var isSuccess = false

        // 콜백들을 위한 슬롯
        val slot = slot<SdpObserver>()
        every { mockPeerConnection.setRemoteDescription(capture(slot), any()) } answers {
            slot.captured.onSetSuccess()
            slot.captured.onCreateSuccess(SessionDescription(SessionDescription.Type.OFFER, ""))
        }

        iceCandidateManagerImpl.setRemoteDescriptionPeerConnection(
            json = JSONObject().put("sdp", "sample sdp"),
            sessionDescriptionType = SessionDescription.Type.OFFER,
            callbackOnSetSuccess = { isSuccess = true },
            callbackOnFailure = {}
        )

        assertTrue(isSuccess)
    }

    @Test
    fun testFailSetRemoteDescriptionPeerConnection() {
        var isSuccess = true

        // 콜백들을 위한 슬롯
        val slot = slot<SdpObserver>()
        every { mockPeerConnection.setRemoteDescription(capture(slot), any()) } answers {
            slot.captured.onSetFailure("testFailure")
            slot.captured.onCreateFailure("testFailure")
        }

        iceCandidateManagerImpl.setRemoteDescriptionPeerConnection(
            json = JSONObject().put("sdp", "sample sdp"),
            sessionDescriptionType = SessionDescription.Type.OFFER,
            callbackOnSetSuccess = {},
            callbackOnFailure = { isSuccess = false }
        )

        assertFalse(isSuccess)
    }

    @Test
    fun testAddIceCandidatePeerConnection() {
        val slot = slot<AddIceObserver>()
        every { mockPeerConnection.addIceCandidate(any(), capture(slot)) } answers {
            slot.captured.onAddSuccess()
            slot.captured.onAddFailure("testFailure")
        }

        iceCandidateManagerImpl.addIceCandidatePeerConnection(
            JSONObject()
                .put("sdpMid", "")
                .put("sdpMLineIndex", 1)
                .put("candidate", "")
        )
    }
}