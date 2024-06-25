package com.pass.data.service.webrtc

import com.pass.data.manager.capture.AudioCaptureManagerImpl
import com.pass.data.manager.capture.VideoCaptureManagerImpl
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
import org.webrtc.AudioTrack
import org.webrtc.VideoTrack

class WebRtcBroadCasterServiceImplTest {

    private val mockPeerConnectionManager = mockk<PeerConnectionManagerImpl>()
    private val mockIceCandidateManager = mockk<IceCandidateManagerImpl>()
    private val mockSocketConnectionManager = mockk<SocketConnectionManagerImpl>()
    private val mockSocketMessageManager = mockk<SocketMessageManagerImpl>()
    private val mockSdpManager = mockk<SdpManagerImpl>()
    private val mockVideoCaptureManager = mockk<VideoCaptureManagerImpl>()
    private val mockAudioCaptureManager = mockk<AudioCaptureManagerImpl>()
    private val mockVideoTrack = mockk<VideoTrack>()
    private val mockAudioTrack = mockk<AudioTrack>()

    private val webRtcBroadCasterServiceImpl = WebRtcBroadCasterServiceImpl(mockPeerConnectionManager, mockIceCandidateManager, mockSocketConnectionManager, mockSocketMessageManager, mockSdpManager, mockVideoCaptureManager, mockAudioCaptureManager)

    @Test
    fun testSuccessStartBroadcast() {
        var isSuccess = false

        every { mockIceCandidateManager.addIceCandidatePeerConnection(any()) } just runs
        every { mockPeerConnectionManager.disposePeerConnection(any()) } just runs
        every { mockSocketConnectionManager.disconnect() } just runs
        every { mockVideoCaptureManager.startVideoCapture() } returns mockVideoTrack
        every { mockAudioCaptureManager.startAudioCapture() } returns mockAudioTrack
        every { mockPeerConnectionManager.addTrackPeerConnection(any(), any()) } just runs
        every { mockSocketMessageManager.emitMessage("start", "testBroadcastId", any()) } just runs
        every { mockSocketMessageManager.emitMessage("iceCandidate", any(), any(), any()) } just runs

        val slotHandleError = slot<() -> Unit>()
        val slotHandleAnswer = slot<(JSONObject) -> Unit>()
        val slotHandleRemoteIceCandidate = slot<(JSONObject) -> Unit>()
        val slotHandleOffer = slot<(JSONObject) -> Unit>()
        every { mockSocketConnectionManager.initializeSocket(any(), capture(slotHandleRemoteIceCandidate), capture(slotHandleError), capture(slotHandleAnswer), capture(slotHandleOffer)) } answers {
            slotHandleRemoteIceCandidate.captured.invoke(JSONObject())
            slotHandleOffer.captured.invoke(JSONObject())
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
        every { mockSdpManager.createOfferPeerConnection(capture(slotCallbackOnSetSuccess), any()) } answers {
            slotCallbackOnSetSuccess.captured.invoke("")
        }

        val slotOnSuccess = slot<() -> Unit>()
        every { mockIceCandidateManager.setRemoteDescriptionPeerConnection(any(), any(), capture(slotOnSuccess), any()) } answers {
            slotOnSuccess.captured.invoke()
        }

        webRtcBroadCasterServiceImpl.startBroadcast(
            broadcastId = "testBroadcastId",
            callbackOnFailureConnected = {},
            callbackOnSuccessConnected = { isSuccess = true }
        )

        slotHandleAnswer.captured.invoke(JSONObject())
        slotCallbackOnIceCandidate.captured.invoke(JSONObject())
        slotOnSuccess.captured.invoke()

        assertTrue(isSuccess)
    }

    @Test
    fun testFailStartBroadCast() {
        var isFail = false

        every { mockIceCandidateManager.addIceCandidatePeerConnection(any()) } just runs
        every { mockPeerConnectionManager.disposePeerConnection(any()) } just runs
        every { mockSocketConnectionManager.disconnect() } just runs
        every { mockVideoCaptureManager.startVideoCapture() } returns mockVideoTrack
        every { mockAudioCaptureManager.startAudioCapture() } returns mockAudioTrack
        every { mockPeerConnectionManager.addTrackPeerConnection(any(), any()) } just runs
        every { mockSocketMessageManager.emitMessage("start", "testBroadcastId", any()) } just runs
        every { mockIceCandidateManager.setRemoteDescriptionPeerConnection(any(), any(), any(), any()) } just runs
        every { mockSocketMessageManager.emitMessage("iceCandidate", "testBroadcastId", any()) } just runs

        val slotHandleError = slot<() -> Unit>()
        val slotHandleAnswer = slot<(JSONObject) -> Unit>()
        val slotHandleRemoteIceCandidate = slot<(JSONObject) -> Unit>()
        every { mockSocketConnectionManager.initializeSocket(any(), any(), capture(slotHandleError), capture(slotHandleAnswer), capture(slotHandleRemoteIceCandidate)) } answers {
            slotHandleError.captured.invoke()
        }

        val slotCallbackOnIceCandidate = slot<(JSONObject) -> Unit>()
        every { mockPeerConnectionManager.createPeerConnection(capture(slotCallbackOnIceCandidate), any(), any()) } just runs

        val slotOnEventConnect = slot<() -> Unit>()
        every { mockSocketConnectionManager.connect(capture(slotOnEventConnect), any()) } just runs

        val slotCallbackOnSetSuccess = slot<(String?) -> Unit>()
        every { mockSdpManager.createOfferPeerConnection(capture(slotCallbackOnSetSuccess), any()) } just runs

        webRtcBroadCasterServiceImpl.startBroadcast(
            broadcastId = "testBroadcastId",
            callbackOnFailureConnected = { isFail = true },
            callbackOnSuccessConnected = {}
        )

        assertTrue(isFail)
    }

    @Test
    fun testSuccessStopBroadcast() {
        val slot = slot<() -> Unit>()
        every { mockVideoCaptureManager.stopVideoCapture() } just runs
        every { mockSocketMessageManager.emitMessage("stop", any()) } just runs
        every { mockPeerConnectionManager.disposePeerConnection(capture(slot)) } answers { slot.captured.invoke() }
        every { mockSocketConnectionManager.disconnect() } just runs

        webRtcBroadCasterServiceImpl.stopBroadcast("testBroadcastId")

        verify { mockVideoCaptureManager.stopVideoCapture() }
        verify { mockSocketMessageManager.emitMessage("stop", any()) }
        verify { mockPeerConnectionManager.disposePeerConnection(any()) }
        verify { mockSocketConnectionManager.disconnect() }
    }
}