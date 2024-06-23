package com.pass.data.service.database

import android.graphics.Bitmap
import com.google.firebase.firestore.DocumentSnapshot
import com.pass.data.di.DateTimeProvider
import com.pass.data.manager.database.FirebaseDatabaseManagerImpl
import com.pass.data.manager.database.FirebaseStorageManagerImpl
import com.pass.data.util.CalculateUtil
import com.pass.data.util.FireStoreUtil
import com.pass.domain.model.LiveStreaming
import com.pass.domain.model.Profile
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Test

class LiveStreamingServiceImplTest {

    private val mockFirebaseDatabaseManager = mockk<FirebaseDatabaseManagerImpl>()
    private val mockFireStoreUtil = mockk<FireStoreUtil>()
    private val mockFirebaseStorageManager = mockk<FirebaseStorageManagerImpl>()
    private val mockCalculateUtil = mockk<CalculateUtil>()
    private val mockDateTimeProvider = mockk<DateTimeProvider>()

    private val mockDocumentSnapshot = mockk<DocumentSnapshot>()
    private val mockBitmap = mockk<Bitmap>()

    private val liveStreamingServiceImpl = LiveStreamingServiceImpl(mockFirebaseDatabaseManager, mockFireStoreUtil, mockFirebaseStorageManager, mockCalculateUtil, mockDateTimeProvider)

    @Test
    fun testSuccessGetLiveStreamingList() = runBlocking {
        coEvery { mockFirebaseDatabaseManager.readDataList("liveStreams") } returns flowOf(Result.success(
            listOf(mockDocumentSnapshot, mockDocumentSnapshot, mockDocumentSnapshot)
        ))

        every { mockFireStoreUtil.extractUserIdsFromDocuments(any()) } returns listOf("testId")

        coEvery { mockFirebaseDatabaseManager.readIdList(any()) } returns flowOf(Result.success(
            listOf(mockDocumentSnapshot)
        ))

        every { mockFireStoreUtil.extractIdOfProfileMapFromDocuments(any()) } returns mapOf("testString" to Profile("", "", emptyList()))
        every { mockFireStoreUtil.extractLiveStreamingListInfoFromIdMapAndDocuments(any(), any(), any()) } answers {
            thirdArg<(String) -> String>().invoke("testString")
            listOf(LiveStreaming("testBroadCastId", "", "", "", ""))
        }
        every { mockCalculateUtil.calculateAgoTime(any()) } returns "testTime"

        val result = liveStreamingServiceImpl.getLiveStreamingList().first()

        assertTrue(result.isSuccess)
        assertEquals(result.getOrNull()?.get(0)?.broadcastId ?: "testFailId", "testBroadCastId")
    }

    @Test
    fun testFailGetLiveStreamingListWithMediaBaseServiceImplGetMediaAndIdList() = runBlocking {
        coEvery { mockFirebaseDatabaseManager.readDataList("liveStreams") } returns flowOf(Result.failure(Exception("test failed")))

        val result = liveStreamingServiceImpl.getLiveStreamingList().first()
        assertTrue(result.isFailure)
        assertEquals(result.exceptionOrNull()?.message, "test failed")
    }

    @Test
    fun testFailGetLiveStreamingListWithMediaBaseServiceImplGetIdList() = runBlocking {
        coEvery { mockFirebaseDatabaseManager.readDataList("liveStreams") } returns flowOf(Result.success(
            listOf(mockDocumentSnapshot, mockDocumentSnapshot, mockDocumentSnapshot)
        ))
        every { mockFireStoreUtil.extractUserIdsFromDocuments(any()) } returns listOf("testId")
        coEvery { mockFirebaseDatabaseManager.readIdList(any()) } returns flowOf(Result.failure(Exception("test failed")))

        val result = liveStreamingServiceImpl.getLiveStreamingList().first()
        assertTrue(result.isFailure)
        assertEquals(result.exceptionOrNull()?.message, "test failed")
    }

    @Test
    fun testSuccessCreateLiveStreamingData() = runBlocking {
        coEvery { mockFirebaseStorageManager.updateFileWithBitmap(any(), "live_streaming_thumbnail/testBroadcastId") } returns flowOf(Result.success("testThumbnail"))
        every { mockDateTimeProvider.localDateTimeNowFormat() } returns "testNowDateTime"
        every { mockFireStoreUtil.createBroadcastData(any(), any(), any(), any()) } returns hashMapOf(
            "userId" to "testBroadcastId",
            "title" to "testTitle",
            "startTime" to "testStartTime",
            "liveThumbnailUri" to "testLiveThumbnailUri",
        )
        coEvery { mockFirebaseDatabaseManager.createData(any(), any(), any()) } returns flowOf(Result.success(Unit))

        val result = liveStreamingServiceImpl.createLiveStreamingData("testBroadcastId", "testTitle", mockBitmap).first()
        assertTrue(result.isSuccess)
    }

    @Test
    fun testFailCreateLiveStreamingDataWithFailUpdateFileWithBitmap() = runBlocking {
        coEvery { mockFirebaseStorageManager.updateFileWithBitmap(any(), "live_streaming_thumbnail/testBroadcastId") } returns flowOf(Result.failure(Exception("test failed")))
        val result = liveStreamingServiceImpl.createLiveStreamingData("testBroadcastId", "testTitle", mockBitmap).first()
        assertTrue(result.isFailure)
        assertEquals(result.exceptionOrNull()?.message, "test failed")
    }

    @Test
    fun testFailCreateLiveStreamingDataWithFailCreateData() = runBlocking {
        coEvery { mockFirebaseStorageManager.updateFileWithBitmap(any(), "live_streaming_thumbnail/testBroadcastId") } returns flowOf(Result.success("testThumbnail"))
        every { mockDateTimeProvider.localDateTimeNowFormat() } returns "testNowDateTime"
        every { mockFireStoreUtil.createBroadcastData(any(), any(), any(), any()) } returns hashMapOf(
            "userId" to "testBroadcastId",
            "title" to "testTitle",
            "startTime" to "testStartTime",
            "liveThumbnailUri" to "testLiveThumbnailUri",
        )
        coEvery { mockFirebaseDatabaseManager.createData(any(), any(), any()) } returns flowOf(Result.failure(Exception("test failed")))

        val result = liveStreamingServiceImpl.createLiveStreamingData("testBroadcastId", "testTitle", mockBitmap).first()
        assertTrue(result.isFailure)
        assertEquals(result.exceptionOrNull()?.message, "test failed")
    }

    @Test
    fun testSuccessDeleteLiveStreamingData() = runBlocking {
        coEvery { mockFirebaseDatabaseManager.deleteData("liveStreams", "testBroadcastId") } returns flowOf(Result.success(Unit))

        liveStreamingServiceImpl.deleteLiveStreamingData("testBroadcastId")

        coVerify { mockFirebaseDatabaseManager.deleteData("liveStreams", "testBroadcastId") }
    }
}