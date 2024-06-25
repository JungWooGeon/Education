package com.pass.data.repository

import android.graphics.Bitmap
import com.google.firebase.auth.FirebaseAuth
import com.pass.data.service.database.VideoServiceImpl
import com.pass.data.util.MediaUtil
import com.pass.domain.model.Video
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Test

class VideoRepositoryImplTest {

    private val mockFirebaseAuth = mockk<FirebaseAuth>()
    private val mockMediaUtil = mockk<MediaUtil>()
    private val mockVideoService = mockk<VideoServiceImpl>()
    private val mockBitmap = mockk<Bitmap>()
    private val mockVideo = mockk<Video>()

    private val videoRepositoryImpl = VideoRepositoryImpl(mockFirebaseAuth, mockMediaUtil, mockVideoService)

    @Test
    fun testSuccessCreateVideoThumbnail() = runBlocking {
        every { mockMediaUtil.extractFirstFrameFromVideoUri(any()) } returns Result.success(mockBitmap)

        val result = videoRepositoryImpl.createVideoThumbnail("testVideoUri")
        assertTrue(result.isSuccess)
        assertEquals(result.getOrNull(), mockBitmap)
    }

    @Test
    fun testSuccessAddVideo() = runBlocking {
        every { mockFirebaseAuth.currentUser?.uid } returns "testUid"
        coEvery { mockVideoService.addVideo(any(), any(), any(), any()) } returns flowOf(Result.success(Unit))

        val result = videoRepositoryImpl.addVideo("testVideoUri", mockBitmap, "testTitle").first()
        assertTrue(result.isSuccess)
    }

    @Test
    fun testFailAddVideoWithNullUid() = runBlocking {
        every { mockFirebaseAuth.currentUser?.uid } returns null

        val result = videoRepositoryImpl.addVideo("testVideoUri", mockBitmap, "testTitle").first()
        assertTrue(result.isFailure)
        assertEquals(result.exceptionOrNull()?.message, "오류가 발생하였습니다. 다시 로그인을 진행해주세요.")
    }

    @Test
    fun testSuccessDeleteVideo() = runBlocking {
        every { mockFirebaseAuth.currentUser?.uid } returns "testUid"
        coEvery { mockVideoService.deleteVideo(any(), any()) } returns flowOf(Result.success(Unit))

        val result = videoRepositoryImpl.deleteVideo(mockVideo).first()
        assertTrue(result.isSuccess)
    }

    @Test
    fun testFailDeleteVideoWithNullUid() = runBlocking {
        every { mockFirebaseAuth.currentUser?.uid } returns null

        val result = videoRepositoryImpl.deleteVideo(mockVideo).first()
        assertTrue(result.isFailure)
        assertEquals(result.exceptionOrNull()?.message, "오류가 발생하였습니다. 다시 로그인을 진행해주세요.")
    }

    @Test
    fun testSuccessGetAllVideoList() = runBlocking {
        coEvery { mockVideoService.getAllVideoList() } returns flowOf(Result.success(emptyList()))

        val result = videoRepositoryImpl.getAllVideoList().first()
        assertTrue(result.isSuccess)
    }
}