package com.pass.data.service.webrtc

import com.pass.data.manager.socket.SocketConnectionManagerImpl
import com.pass.data.manager.socket.SocketMessageManagerImpl
import com.pass.data.manager.webrtc.IceCandidateManagerImpl
import com.pass.data.manager.webrtc.PeerConnectionManagerImpl
import com.pass.data.manager.webrtc.SdpManagerImpl
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import junit.framework.TestCase.assertTrue
import org.json.JSONObject
import org.junit.Test
import org.webrtc.VideoTrack

class WebRtcViewerServiceImplTest {

    private val mockPeerConnectionManager = mockk<PeerConnectionManagerImpl>()
    private val mockIceCandidateManager = mockk<IceCandidateManagerImpl>()
    private val mockSocketConnectionManager = mockk<SocketConnectionManagerImpl>()
    private val mockSocketMessageManager = mockk<SocketMessageManagerImpl>()
    private val mockSdpManager = mockk<SdpManagerImpl>()
    private val mockVideoTrack = mockk<VideoTrack>()

    private val webRtcViewerServiceImpl = WebRtcViewerServiceImpl(mockPeerConnectionManager, mockIceCandidateManager, mockSocketConnectionManager, mockSocketMessageManager, mockSdpManager)

    @Test
    fun testStartViewing() {
        var isSuccess = false
        var isFail = true

        every { mockIceCandidateManager.addIceCandidatePeerConnection(any()) } just runs
        every { mockPeerConnectionManager.disposePeerConnection(any()) } just runs
        every { mockSocketConnectionManager.disconnect() } just runs
        every { mockSocketMessageManager.emitMessage("join", "testBroadcastId") } just runs
        every { mockSocketMessageManager.emitMessage("iceCandidate", any(), any(), any()) } just runs
        every { mockSocketMessageManager.emitMessage("answer", "testBroadcastId", any()) } just runs

        val slotHandleError = slot<() -> Unit>()
        val slotHandleAnswer = slot<(JSONObject) -> Unit>()
        val slotHandleRemoteIceCandidate = slot<(JSONObject) -> Unit>()
        val slotHandleOffer = slot<(JSONObject) -> Unit>()
        every { mockSocketConnectionManager.initializeSocket(any(), capture(slotHandleRemoteIceCandidate), capture(slotHandleError), capture(slotHandleAnswer), capture(slotHandleOffer)) } answers {
            slotHandleRemoteIceCandidate.captured.invoke(JSONObject())
            slotHandleAnswer.captured.invoke(JSONObject())
            slotHandleOffer.captured.invoke(JSONObject())
            slotHandleError.captured.invoke()
        }

        val slotCallbackOnIceCandidate = slot<(JSONObject) -> Unit>()
        val slotCallbackOnReceiveVideoTrack = slot<(VideoTrack) -> Unit>()
        every { mockPeerConnectionManager.createPeerConnection(capture(slotCallbackOnIceCandidate), capture(slotCallbackOnReceiveVideoTrack), any()) } answers {
            slotCallbackOnIceCandidate.captured.invoke(JSONObject())
            slotCallbackOnReceiveVideoTrack.captured.invoke(mockVideoTrack)
        }

        val slotOnEventConnect = slot<() -> Unit>()
        every { mockSocketConnectionManager.connect(capture(slotOnEventConnect), any()) } answers {
            slotOnEventConnect.captured.invoke()
        }

        val slotCallbackOnSetSuccess = slot<(String?) -> Unit>()
        every { mockSdpManager.createAnswerPeerConnection(capture(slotCallbackOnSetSuccess), any()) } answers {
            slotCallbackOnSetSuccess.captured.invoke("")
        }

        val slotOnSuccess = slot<() -> Unit>()
        every { mockIceCandidateManager.setRemoteDescriptionPeerConnection(any(), any(), capture(slotOnSuccess), any()) } answers {
            slotOnSuccess.captured.invoke()
        }

        webRtcViewerServiceImpl.startViewing(
            broadcastId = "testBroadcastId",
            callbackOnFailureConnected = { isFail = true },
            callbackOnSuccessConnected = { isSuccess = true }
        )

        assertTrue(isSuccess)
        assertTrue(isFail)
    }

    @Test
    fun testSuccessStopViewing() {
        val slot = slot<() -> Unit>()
        every { mockPeerConnectionManager.disposePeerConnection(capture(slot)) } answers { slot.captured.invoke() }
        every { mockSocketConnectionManager.disconnect() } just runs

        webRtcViewerServiceImpl.stopViewing()

        verify { mockPeerConnectionManager.disposePeerConnection(any()) }
        verify { mockSocketConnectionManager.disconnect() }
    }
}