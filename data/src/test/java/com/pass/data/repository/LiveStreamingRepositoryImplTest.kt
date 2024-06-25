package com.pass.data.repository

import android.graphics.Bitmap
import com.pass.data.service.auth.AuthenticationServiceImpl
import com.pass.data.service.database.LiveStreamingServiceImpl
import com.pass.data.service.webrtc.WebRtcBroadCasterServiceImpl
import com.pass.data.service.webrtc.WebRtcViewerServiceImpl
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.webrtc.VideoTrack

class LiveStreamingRepositoryImplTest {

    private val mockWebRtcBroadCasterService = mockk<WebRtcBroadCasterServiceImpl>()
    private val mockWebRtcViewerService = mockk<WebRtcViewerServiceImpl>()
    private val mockLiveStreamingService = mockk<LiveStreamingServiceImpl>()
    private val mockAuthenticationService = mockk<AuthenticationServiceImpl>()
    private val mockThumbnailImage = mockk<Bitmap>()
    private val mockVideoTrack = mockk<VideoTrack>()

    private val liveStreamingRepositoryImpl = LiveStreamingRepositoryImpl(mockWebRtcBroadCasterService, mockWebRtcViewerService, mockLiveStreamingService, mockAuthenticationService)

    @Test
    fun testFailGetLiveStreamingList() = runBlocking {
        coEvery { mockLiveStreamingService.getLiveStreamingList() } returns flowOf(Result.failure(Exception("")))

        val result = liveStreamingRepositoryImpl.getLiveStreamingList().first()
        assertTrue(result.isFailure)
    }

    @Test
    fun testStartLiveStreaming() = runBlocking {
        coEvery { mockAuthenticationService.getCurrentUserId() } returns "testUserId"
        coEvery { mockLiveStreamingService.createLiveStreamingData(any(), any(), any()) } returns flowOf(Result.success(Unit))
        val slotCallbackOnSuccessConnected = slot<(VideoTrack) -> Unit>()
        coEvery { mockWebRtcBroadCasterService.startBroadcast(any(), any(), capture(slotCallbackOnSuccessConnected)) } answers {
            slotCallbackOnSuccessConnected.captured.invoke(mockVideoTrack)
        }

        val result = liveStreamingRepositoryImpl.startLiveStreaming("testTitle", mockThumbnailImage).first()
        assertTrue(result.isSuccess)
    }

    @Test
    fun testFailStartLiveStreamingWithNullUid() = runBlocking {
        coEvery { mockAuthenticationService.getCurrentUserId() } returns null

        val result = liveStreamingRepositoryImpl.startLiveStreaming("testTitle", mockThumbnailImage).first()
        assertTrue(result.isFailure)
        assertEquals(result.exceptionOrNull()?.message, "오류가 발생하였습니다. 다시 로그인을 진행해주세요.")
    }

    @Test
    fun testFailStartLiveStreamingWithFailCreateLiveStreamingData() = runBlocking {
        coEvery { mockAuthenticationService.getCurrentUserId() } returns "testUserId"
        coEvery { mockLiveStreamingService.createLiveStreamingData(any(), any(), any()) } returns flowOf(Result.failure(Exception("test failed")))

        val result = liveStreamingRepositoryImpl.startLiveStreaming("testTitle", mockThumbnailImage).first()
        assertTrue(result.isFailure)
        assertEquals(result.exceptionOrNull()?.message, "test failed")
    }

    @Test
    fun testFailStartLiveStreamingWithFailStartWebRtc() = runBlocking {
        coEvery { mockAuthenticationService.getCurrentUserId() } returns "testUserId"
        coEvery { mockLiveStreamingService.createLiveStreamingData(any(), any(), any()) } returns flowOf(Result.success(Unit))
        val slotCallbackOnFailureConnected = slot<() -> Unit>()
        coEvery { mockWebRtcBroadCasterService.startBroadcast(any(), capture(slotCallbackOnFailureConnected), any()) } answers {
            slotCallbackOnFailureConnected.captured.invoke()
        }

        val result = liveStreamingRepositoryImpl.startLiveStreaming("testTitle", mockThumbnailImage).first()
        assertTrue(result.isFailure)
        assertEquals(result.exceptionOrNull()?.message, "Failed to connect to the broadcast.")
    }

    @Test
    fun testSuccessWatchLiveStreaming() = runBlocking {
        val slotCallbackOnSuccessConnected = slot<(VideoTrack) -> Unit>()
        coEvery { mockWebRtcViewerService.startViewing(any(), any(), capture(slotCallbackOnSuccessConnected)) } answers {
            slotCallbackOnSuccessConnected.captured.invoke(mockVideoTrack)
        }

        val result = liveStreamingRepositoryImpl.watchLiveStreaming("testBroadcastId").first()
        assertTrue(result.isSuccess)
    }

    @Test
    fun testFailWatchLiveStreaming() = runBlocking {
        val slotCallbackOnFailureConnected = slot<() -> Unit>()
        coEvery { mockWebRtcViewerService.startViewing(any(), capture(slotCallbackOnFailureConnected), any()) } answers {
            slotCallbackOnFailureConnected.captured.invoke()
        }

        val result = liveStreamingRepositoryImpl.watchLiveStreaming("testBroadcastId").first()
        assertTrue(result.isFailure)
        assertEquals(result.exceptionOrNull()?.message, "방송이 종료되었습니다.")
    }

    @Test
    fun testSuccessStopLiveStreaming() = runBlocking {
        coEvery { mockAuthenticationService.getCurrentUserId() } returns "testUserId"
        coEvery { mockLiveStreamingService.deleteLiveStreamingData(any()) } just runs
        coEvery { mockWebRtcBroadCasterService.stopBroadcast(any()) } just runs

        liveStreamingRepositoryImpl.stopLiveStreaming()

        coVerify { mockAuthenticationService.getCurrentUserId() }
        coVerify { mockLiveStreamingService.deleteLiveStreamingData(any()) }
        coVerify { mockWebRtcBroadCasterService.stopBroadcast(any()) }
    }

    @Test
    fun testSuccessStopViewing() = runBlocking {
        coEvery { mockWebRtcViewerService.stopViewing() } just runs

        liveStreamingRepositoryImpl.stopViewing()

        coVerify { mockWebRtcViewerService.stopViewing() }
    }
}