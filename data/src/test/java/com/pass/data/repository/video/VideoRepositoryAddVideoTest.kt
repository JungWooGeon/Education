package com.pass.data.repository.video

import android.content.Context
import android.media.MediaMetadataRetriever
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.pass.data.di.DateTimeProvider
import com.pass.data.repository.VideoRepositoryImpl
import com.pass.data.util.CalculateUtil
import com.pass.domain.util.DatabaseUtil
import com.pass.domain.util.StorageUtil
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayOutputStream

class VideoRepositoryAddVideoTest {

    private val mockFirebaseAuth = mockk<FirebaseAuth>()
    private val mockkDatabaseUtil = mockk<DatabaseUtil<DocumentSnapshot>>()
    private val mockStorageUtil = mockk<StorageUtil>()
    private val mockCalculateUtil = mockk<CalculateUtil>()
    private val mockContext = mockk<Context>()
    private val mockMediaMetadataRetriever = mockk<MediaMetadataRetriever>()
    private val mockByteArrayOutputStream = mockk<ByteArrayOutputStream>()
    private val mockString = "mockString"

    private val testVideoUri = "testUri"
    private val testVideoThumbnailBitmap = "testBitmap"
    private val testTitle = "testTitle"
    private val testUid = "test uid"

    private val videoRepositoryImpl = VideoRepositoryImpl(
        auth = mockFirebaseAuth,
        firebaseDatabaseUtil = mockkDatabaseUtil,
        firebaseStorageUtil = mockStorageUtil,
        context = mockContext,
        calculateUtil = mockCalculateUtil,
        mediaMetadataRetriever = mockMediaMetadataRetriever,
        byteArrayOutputStream = mockByteArrayOutputStream,
        dateTimeProvider = DateTimeProvider()
    )

    @Test
    fun testSuccessAddVideo() = runBlocking {
        every { mockFirebaseAuth.currentUser?.uid } returns testUid

        val successFlow: Flow<Result<String>> = flow {
            emit(Result.success(mockString))
        }
        coEvery { mockStorageUtil.updateFile(any(), any()) } returns successFlow
        coEvery { mockStorageUtil.updateFileWithBitmap(any(), any()) } returns successFlow

        val successSubFlow: Flow<Result<Unit>> = flow {
            emit(Result.success(Unit))
        }
        coEvery { mockkDatabaseUtil.createData(any(), any(), any(), any(), any()) } returns successSubFlow
        coEvery { mockkDatabaseUtil.createData(any(), any(), any()) } returns successSubFlow

        val result = videoRepositoryImpl.addVideo(
            videoUri = testVideoUri,
            videoThumbnailBitmap = testVideoThumbnailBitmap,
            title = testTitle
        ).first()

        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull() == true)
    }

    @Test
    fun testFailAddVideoWithUidNull() = runBlocking {
        every { mockFirebaseAuth.currentUser?.uid } returns null

        val result = videoRepositoryImpl.addVideo(
            videoUri = testVideoUri,
            videoThumbnailBitmap = testVideoThumbnailBitmap,
            title = testTitle
        ).first()

        assertTrue(result.isFailure)
        assertEquals(result.exceptionOrNull()?.message, "오류가 발생하였습니다. 다시 로그인을 진행해주세요.")
    }

    @Test
    fun testFailAddVideoWithFailUpdateFileFlow() = runBlocking {
        val testException = Exception("test exception")

        every { mockFirebaseAuth.currentUser?.uid } returns testUid

        val successFlow: Flow<Result<String>> = flow {
            emit(Result.success(mockString))
        }
        val failFlow: Flow<Result<String>> = flow {
            emit(Result.failure(testException))
        }
        coEvery { mockStorageUtil.updateFile(any(), any()) } returns failFlow
        coEvery { mockStorageUtil.updateFileWithBitmap(any(), any()) } returns successFlow

        val successSubFlow: Flow<Result<Unit>> = flow {
            emit(Result.success(Unit))
        }
        coEvery { mockkDatabaseUtil.createData(any(), any(), any(), any(), any()) } returns successSubFlow
        coEvery { mockkDatabaseUtil.createData(any(), any(), any()) } returns successSubFlow

        val result = videoRepositoryImpl.addVideo(
            videoUri = testVideoUri,
            videoThumbnailBitmap = testVideoThumbnailBitmap,
            title = testTitle
        ).first()

        assertTrue(result.isFailure)
        assertEquals(result.exceptionOrNull()?.message, testException.message)
    }

    @Test
    fun testFailAddVideoWithFailUpdateFileWithBitmap() = runBlocking {
        val testException = Exception("test exception")

        every { mockFirebaseAuth.currentUser?.uid } returns testUid

        val successFlow: Flow<Result<String>> = flow {
            emit(Result.success(mockString))
        }
        val failFlow: Flow<Result<String>> = flow {
            emit(Result.failure(testException))
        }
        coEvery { mockStorageUtil.updateFile(any(), any()) } returns successFlow
        coEvery { mockStorageUtil.updateFileWithBitmap(any(), any()) } returns failFlow

        val successSubFlow: Flow<Result<Unit>> = flow {
            emit(Result.success(Unit))
        }
        coEvery { mockkDatabaseUtil.createData(any(), any(), any(), any(), any()) } returns successSubFlow
        coEvery { mockkDatabaseUtil.createData(any(), any(), any()) } returns successSubFlow

        val result = videoRepositoryImpl.addVideo(
            videoUri = testVideoUri,
            videoThumbnailBitmap = testVideoThumbnailBitmap,
            title = testTitle
        ).first()

        assertTrue(result.isFailure)
        assertEquals(result.exceptionOrNull()?.message, testException.message)
    }

    @Test
    fun testFailAddVideoWithFailProfileVideoFlow() = runBlocking {
        val testException = Exception("test exception")

        every { mockFirebaseAuth.currentUser?.uid } returns testUid

        val successFlow: Flow<Result<String>> = flow {
            emit(Result.success(mockString))
        }
        coEvery { mockStorageUtil.updateFile(any(), any()) } returns successFlow
        coEvery { mockStorageUtil.updateFileWithBitmap(any(), any()) } returns successFlow

        val successSubFlow: Flow<Result<Unit>> = flow {
            emit(Result.success(Unit))
        }
        val failSubFlow: Flow<Result<Unit>> = flow {
            emit(Result.failure(testException))
        }
        coEvery { mockkDatabaseUtil.createData(any(), any(), any(), any(), any()) } returns failSubFlow
        coEvery { mockkDatabaseUtil.createData(any(), any(), any()) } returns successSubFlow

        val result = videoRepositoryImpl.addVideo(
            videoUri = testVideoUri,
            videoThumbnailBitmap = testVideoThumbnailBitmap,
            title = testTitle
        ).first()

        assertTrue(result.isFailure)
        assertEquals(result.exceptionOrNull()?.message, testException.message)
    }

    @Test
    fun testFailAddVideoWithFailAllVideoFlow() = runBlocking {
        val testException = Exception("test exception")

        every { mockFirebaseAuth.currentUser?.uid } returns testUid

        val successFlow: Flow<Result<String>> = flow {
            emit(Result.success(mockString))
        }
        coEvery { mockStorageUtil.updateFile(any(), any()) } returns successFlow
        coEvery { mockStorageUtil.updateFileWithBitmap(any(), any()) } returns successFlow

        val successSubFlow: Flow<Result<Unit>> = flow {
            emit(Result.success(Unit))
        }
        val failSubFlow: Flow<Result<Unit>> = flow {
            emit(Result.failure(testException))
        }
        coEvery { mockkDatabaseUtil.createData(any(), any(), any(), any(), any()) } returns successSubFlow
        coEvery { mockkDatabaseUtil.createData(any(), any(), any()) } returns failSubFlow

        val result = videoRepositoryImpl.addVideo(
            videoUri = testVideoUri,
            videoThumbnailBitmap = testVideoThumbnailBitmap,
            title = testTitle
        ).first()

        assertTrue(result.isFailure)
        assertEquals(result.exceptionOrNull()?.message, testException.message)
    }
}