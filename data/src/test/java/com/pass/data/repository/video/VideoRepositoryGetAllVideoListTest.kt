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
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayOutputStream

class VideoRepositoryGetAllVideoListTest {

    private val mockFirebaseAuth = mockk<FirebaseAuth>()
    private val mockkDatabaseUtil = mockk<DatabaseUtil<DocumentSnapshot>>()
    private val mockStorageUtil = mockk<StorageUtil>()
    private val mockCalculateUtil = mockk<CalculateUtil>()
    private val mockContext = mockk<Context>()
    private val mockMediaMetadataRetriever = mockk<MediaMetadataRetriever>()
    private val mockByteArrayOutputStream = mockk<ByteArrayOutputStream>()

    private val mockListDocumentSnapshot = listOf<DocumentSnapshot>()

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
    fun testSuccessGetAllVideoList() = runBlocking {
        val successFlow: Flow<Result<List<DocumentSnapshot>>> = flow {
            emit(Result.success(mockListDocumentSnapshot))
        }

        coEvery { mockkDatabaseUtil.readDataList(any()) } returns successFlow
        coEvery { mockkDatabaseUtil.readIdList(any()) } returns successFlow

        val result = videoRepositoryImpl.getAllVideoList().first()

        assertTrue(result.isSuccess)
    }

    @Test
    fun testFailGetAllVideoListWithFailReadDataList() = runBlocking {
        val testException = Exception("test exception")

        val successFlow: Flow<Result<List<DocumentSnapshot>>> = flow {
            emit(Result.success(mockListDocumentSnapshot))
        }
        val failFlow: Flow<Result<List<DocumentSnapshot>>> = flow {
            emit(Result.failure(testException))
        }

        coEvery { mockkDatabaseUtil.readDataList(any()) } returns failFlow
        coEvery { mockkDatabaseUtil.readIdList(any()) } returns successFlow

        val result = videoRepositoryImpl.getAllVideoList().first()

        assertTrue(result.isFailure)
        assertEquals(result.exceptionOrNull()?.message, testException.message)
    }

    @Test
    fun testFailGetAllVideoListWithFailReadIdList() = runBlocking {
        val testException = Exception("test exception")

        val successFlow: Flow<Result<List<DocumentSnapshot>>> = flow {
            emit(Result.success(mockListDocumentSnapshot))
        }
        val failFlow: Flow<Result<List<DocumentSnapshot>>> = flow {
            emit(Result.failure(testException))
        }

        coEvery { mockkDatabaseUtil.readDataList(any()) } returns successFlow
        coEvery { mockkDatabaseUtil.readIdList(any()) } returns failFlow

        val result = videoRepositoryImpl.getAllVideoList().first()

        assertTrue(result.isFailure)
        assertEquals(result.exceptionOrNull()?.message, testException.message)
    }
}