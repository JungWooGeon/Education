package com.pass.data.service.database

import android.graphics.Bitmap
import com.pass.data.di.DateTimeProvider
import com.pass.data.manager.database.FirebaseDatabaseManagerImpl
import com.pass.data.manager.database.FirebaseStorageManagerImpl
import com.pass.data.util.CalculateUtil
import com.pass.data.util.FireStoreUtil
import com.pass.domain.model.Video
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Test

class VideoServiceImplTest {

    private val mockFirebaseDatabaseManager = mockk<FirebaseDatabaseManagerImpl>()
    private val mockFireStoreUtil = mockk<FireStoreUtil>()
    private val mockFirebaseStorageManager = mockk<FirebaseStorageManagerImpl>()
    private val mockDateTimeProvider = mockk<DateTimeProvider>()
    private val mockCalculateUtil = mockk<CalculateUtil>()
    private val mockBitmap = mockk<Bitmap>()
    private val mockVideo = mockk<Video>()

    private val videoServiceImpl = VideoServiceImpl(mockFirebaseDatabaseManager, mockFireStoreUtil, mockFirebaseStorageManager, mockDateTimeProvider, mockCalculateUtil)

    @Test
    fun testSuccessAddVideo() = runBlocking {
        every { mockDateTimeProvider.localDateTimeNowFormat() } returns ""
        coEvery { mockFirebaseStorageManager.updateFile(any(), any()) } returns flowOf(Result.success(""))
        coEvery { mockFirebaseStorageManager.updateFileWithBitmap(any(), any()) } returns flowOf(Result.success(""))
        every { mockFireStoreUtil.createVideoData(any(), any(), any(), any(), any()) } returns hashMapOf()
        coEvery { mockFirebaseDatabaseManager.createData(any(), "profiles", any(), "videos", any()) }  returns flowOf(Result.success(Unit))
        coEvery { mockFirebaseDatabaseManager.createData(any(), "videos", any()) }  returns flowOf(Result.success(Unit))

        val result = videoServiceImpl.addVideo("", mockBitmap, "", "").first()

        assertTrue(result.isSuccess)
    }

    @Test
    fun testFailAddVideoWithFailUpdateFile() = runBlocking {
        every { mockDateTimeProvider.localDateTimeNowFormat() } returns ""
        coEvery { mockFirebaseStorageManager.updateFile(any(), any()) } returns flowOf(Result.failure(Exception("")))
        coEvery { mockFirebaseStorageManager.updateFileWithBitmap(any(), any()) } returns flowOf(Result.success(""))

        val result = videoServiceImpl.addVideo("", mockBitmap, "", "").first()

        assertTrue(result.isFailure)
        assertEquals(result.exceptionOrNull()?.message, "File upload Failed")
    }

    @Test
    fun testFailAddVideoWithFailCreateData() = runBlocking {
        every { mockDateTimeProvider.localDateTimeNowFormat() } returns ""
        coEvery { mockFirebaseStorageManager.updateFile(any(), any()) } returns flowOf(Result.success(""))
        coEvery { mockFirebaseStorageManager.updateFileWithBitmap(any(), any()) } returns flowOf(Result.success(""))
        every { mockFireStoreUtil.createVideoData(any(), any(), any(), any(), any()) } returns hashMapOf()
        coEvery { mockFirebaseDatabaseManager.createData(any(), "profiles", any(), "videos", any()) }  returns flowOf(Result.failure(Exception("")))
        coEvery { mockFirebaseDatabaseManager.createData(any(), "videos", any()) }  returns flowOf(Result.success(Unit))

        val result = videoServiceImpl.addVideo("", mockBitmap, "", "").first()

        assertTrue(result.isFailure)
        assertEquals(result.exceptionOrNull()?.message, "File upload Failed")
    }

    @Test
    fun testSuccessDeleteVideo() = runBlocking {
        every { mockVideo.videoId } returns ""
        coEvery { mockFirebaseStorageManager.deleteFile(any()) } returns flowOf(Result.success(Unit))
        coEvery { mockFirebaseDatabaseManager.deleteData(any(), any(), any(), any()) } returns flowOf(Result.success(Unit))
        coEvery { mockFirebaseDatabaseManager.deleteData(any(), any()) } returns flowOf(Result.success(Unit))

        val result = videoServiceImpl.deleteVideo(mockVideo, "").first()

        assertTrue(result.isSuccess)
    }

    @Test
    fun testFailDeleteVideo() = runBlocking {
        every { mockVideo.videoId } returns ""
        coEvery { mockFirebaseStorageManager.deleteFile(any()) } returns flowOf(Result.success(Unit))
        coEvery { mockFirebaseDatabaseManager.deleteData(any(), any(), any(), any()) } returns flowOf(Result.success(Unit))
        coEvery { mockFirebaseDatabaseManager.deleteData(any(), any()) } returns flowOf(Result.failure(Exception("")))

        val result = videoServiceImpl.deleteVideo(mockVideo, "").first()

        assertTrue(result.isFailure)
        assertEquals(result.exceptionOrNull()?.message, "동영상 삭제에 실패하였습니다.")
    }

    @Test
    fun testSuccessGetAllVideoList() = runBlocking {
        coEvery { mockFirebaseDatabaseManager.readDataList(any()) } returns flowOf(Result.success(emptyList()))
        every { mockFireStoreUtil.extractUserIdsFromDocuments(any()) } returns emptyList()
        coEvery { mockFirebaseDatabaseManager.readIdList(any()) } returns flowOf(Result.success(emptyList()))
        every { mockFireStoreUtil.extractIdOfProfileMapFromDocuments(any()) } returns mapOf()

        every { mockCalculateUtil.calculateAgoTime(any()) } returns ""
        val calculateAgoTimeSlot = slot<(String?) -> String>()
        every { mockFireStoreUtil.extractVideoListInfoFromIdMapAndDocuments(any(), any(), capture(calculateAgoTimeSlot)) } answers {
            calculateAgoTimeSlot.captured.invoke("")
            emptyList()
        }

        val result = videoServiceImpl.getAllVideoList().first()

        assertTrue(result.isSuccess)
    }

    @Test
    fun testFailGetAllVideoListWithFailGetIdList() = runBlocking {
        coEvery { mockFirebaseDatabaseManager.readDataList(any()) } returns flowOf(Result.failure(Exception("test failed")))

        val result = videoServiceImpl.getAllVideoList().first()

        assertTrue(result.isFailure)
        assertEquals(result.exceptionOrNull()?.message, "test failed")
    }

    @Test
    fun testFailGetAllVideoListWithFailGetMediaAndIdList() = runBlocking {
        coEvery { mockFirebaseDatabaseManager.readDataList(any()) } returns flowOf(Result.success(emptyList()))
        every { mockFireStoreUtil.extractUserIdsFromDocuments(any()) } returns emptyList()
        coEvery { mockFirebaseDatabaseManager.readIdList(any()) } returns flowOf(Result.failure(Exception("test failed")))
        every { mockFireStoreUtil.extractIdOfProfileMapFromDocuments(any()) } returns mapOf()
        val calculateAgoTimeSlot = slot<(String?) -> String>()
        every { mockFireStoreUtil.extractVideoListInfoFromIdMapAndDocuments(any(), any(), capture(calculateAgoTimeSlot)) } answers {
            calculateAgoTimeSlot.captured.invoke("")
            emptyList()
        }

        val result = videoServiceImpl.getAllVideoList().first()

        assertTrue(result.isFailure)
        assertEquals(result.exceptionOrNull()?.message, "test failed")
    }
}