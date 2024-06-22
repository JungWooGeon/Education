package com.pass.data.manager.capture

import android.content.Context
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.runs
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.webrtc.CapturerObserver
import org.webrtc.EglBase
import org.webrtc.PeerConnectionFactory
import org.webrtc.SurfaceTextureHelper
import org.webrtc.VideoCapturer
import org.webrtc.VideoSource
import org.webrtc.VideoTrack

@RunWith(RobolectricTestRunner::class)
class VideoCaptureManagerImplTest {

    private val mockPeerConnectionFactory = mockk<PeerConnectionFactory>()
    private val mockVideoCapturerFactory = mockk<VideoCapturerFactory>()
    private val mockEglBaseContext = mockk<EglBase.Context>()
    private val mockContext = mockk<Context>()
    private val mockVideoCapturer = mockk<VideoCapturer>()
    private val mockVideoSource = mockk<VideoSource>()
    private val mockVideoTrack = mockk<VideoTrack>()
    private val mockSurfaceTextureHelper = mockk<SurfaceTextureHelper>()
    private val mockCapturerObserver = mockk<CapturerObserver>()

    private val videoCaptureManagerImpl = VideoCaptureManagerImpl(mockPeerConnectionFactory, mockVideoCapturerFactory, mockEglBaseContext, mockContext)

    @Test
    fun testSuccessStartAndStopVideoCapture() {
        // start
        every { mockVideoCapturerFactory.createVideoCapturer() } returns mockVideoCapturer
        every { mockPeerConnectionFactory.createVideoSource(any()) } returns mockVideoSource
        every { mockVideoCapturer.initialize(any(), any(), any()) } just runs
        every { mockVideoCapturer.isScreencast } returns true
        mockkStatic(SurfaceTextureHelper::class)
        every { SurfaceTextureHelper.create(any(), any()) } returns mockSurfaceTextureHelper
        every { mockVideoSource.capturerObserver } returns mockCapturerObserver
        every { mockVideoCapturer.startCapture(any(), any(), any()) } just runs
        every { mockPeerConnectionFactory.createVideoTrack(any(), any()) } returns mockVideoTrack
        every { mockVideoTrack.setEnabled(any()) } returns true

        val videoTrack = videoCaptureManagerImpl.startVideoCapture()
        assertEquals(videoTrack, mockVideoTrack)

        // stop
        every { mockVideoCapturer.stopCapture() } just runs
        videoCaptureManagerImpl.stopVideoCapture()
        verify { mockVideoCapturer.stopCapture() }
    }
}