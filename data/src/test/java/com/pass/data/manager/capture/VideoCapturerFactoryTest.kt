package com.pass.data.manager.capture

import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.fail
import org.junit.Before
import org.junit.Test
import org.webrtc.Camera2Enumerator
import org.webrtc.CameraVideoCapturer

class VideoCapturerFactoryTest {

    private val mockVideoCapturer = mockk<CameraVideoCapturer>()
    private val mockCamera2Enumerator = mockk<Camera2Enumerator>()

    private val videoCapturerFactory = VideoCapturerFactory(mockCamera2Enumerator)

    @Before
    fun setup() {
        every { mockCamera2Enumerator.deviceNames } returns arrayOf("")
        every { mockCamera2Enumerator.isFrontFacing(any()) } returns true
    }

    @Test
    fun testSuccessCreateVideoCapturer() {
        every { mockCamera2Enumerator.createCapturer(any(), any()) } returns mockVideoCapturer

        val result = videoCapturerFactory.createVideoCapturer()
        assertEquals(result, mockVideoCapturer)
    }

    @Test
    fun testFailCreateVideoCapturer() {
        every { mockCamera2Enumerator.createCapturer(any(), any()) } returns null

        try {
            videoCapturerFactory.createVideoCapturer()
            fail()
        } catch (e: Exception) {
            assertEquals(e.message, "Failed to open front facing camera")
        }
    }
}