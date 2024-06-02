package com.pass.data.repository.video

import android.content.Context
import android.media.MediaMetadataRetriever
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.pass.data.di.DateTimeProvider
import com.pass.data.repository.VideoRepositoryImpl
import com.pass.data.util.CalculateUtil
import com.pass.domain.model.Video
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

class VideoRepositoryDeleteVideoTest {

    private val mockFirebaseAuth = mockk<FirebaseAuth>()
    private val mockkDatabaseUtil = mockk<DatabaseUtil<DocumentSnapshot>>()
    private val mockStorageUtil = mockk<StorageUtil>()
    private val mockCalculateUtil = mockk<CalculateUtil>()
    private val mockContext = mockk<Context>()
    private val mockMediaMetadataRetriever = mockk<MediaMetadataRetriever>()
    private val mockByteArrayOutputStream = mockk<ByteArrayOutputStream>()
    private val mockVideo = Video(
        videoId = "videoId",
        userId = "userId",
        videoThumbnailUrl = "videoThumbnailUrl",
        videoTitle = "videoTitle",
        agoTime = "agoTime",
        videoUrl = "videoUrl",
    )

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
    fun testSuccessDeleteVideo() = runBlocking {
        every { mockFirebaseAuth.currentUser?.uid } returns testUid

        val successDeleteFlow: Flow<Result<Unit>> = flow {
            emit(Result.success(Unit))
        }

        coEvery { mockStorageUtil.deleteFile(any()) } returns successDeleteFlow
        coEvery { mockkDatabaseUtil.deleteData(any(), any(), any(), any()) } returns successDeleteFlow
        coEvery { mockkDatabaseUtil.deleteData(any(), any()) } returns successDeleteFlow

        val result = videoRepositoryImpl.deleteVideo(video = mockVideo).first()

        assertTrue(result.isSuccess)
    }

    @Test
    fun testFailDeleteVideoWithUidNull() = runBlocking {
        every { mockFirebaseAuth.currentUser?.uid } returns null

        val result = videoRepositoryImpl.deleteVideo(video = mockVideo).first()

        assertTrue(result.isFailure)
        assertEquals(result.exceptionOrNull()?.message, "오류가 발생하였습니다. 다시 로그인을 진행해주세요.")
    }

    @Test
    fun testFailDeleteVideoWithFailDeleteFileFlow() = runBlocking {
        every { mockFirebaseAuth.currentUser?.uid } returns testUid

        val successDeleteFlow: Flow<Result<Unit>> = flow {
            emit(Result.success(Unit))
        }
        val failDeleteFlow: Flow<Result<Unit>> = flow {
            emit(Result.failure(Exception()))
        }

        coEvery { mockStorageUtil.deleteFile(any()) } returns failDeleteFlow
        coEvery { mockkDatabaseUtil.deleteData(any(), any(), any(), any()) } returns successDeleteFlow
        coEvery { mockkDatabaseUtil.deleteData(any(), any()) } returns successDeleteFlow

        val result = videoRepositoryImpl.deleteVideo(video = mockVideo).first()

        assertTrue(result.isFailure)
        assertEquals(result.exceptionOrNull()?.message, "동영상 삭제에 실패하였습니다.")
    }

    @Test
    fun testFailDeleteVideoWithFailDeleteDataFlow() = runBlocking {
        every { mockFirebaseAuth.currentUser?.uid } returns testUid

        val successDeleteFlow: Flow<Result<Unit>> = flow {
            emit(Result.success(Unit))
        }
        val failDeleteFlow: Flow<Result<Unit>> = flow {
            emit(Result.failure(Exception()))
        }

        // 첫 번째 DeleteData Flow 테스트
        coEvery { mockStorageUtil.deleteFile(any()) } returns successDeleteFlow
        coEvery { mockkDatabaseUtil.deleteData(any(), any(), any(), any()) } returns failDeleteFlow
        coEvery { mockkDatabaseUtil.deleteData(any(), any()) } returns successDeleteFlow

        val result = videoRepositoryImpl.deleteVideo(video = mockVideo).first()

        assertTrue(result.isFailure)
        assertEquals(result.exceptionOrNull()?.message, "동영상 삭제에 실패하였습니다.")

        // 두 번째 DeleteData Flow 실패 테스트
        coEvery { mockkDatabaseUtil.deleteData(any(), any(), any(), any()) } returns successDeleteFlow
        coEvery { mockkDatabaseUtil.deleteData(any(), any()) } returns failDeleteFlow
        val result2 = videoRepositoryImpl.deleteVideo(video = mockVideo).first()

        assertTrue(result2.isFailure)
        assertEquals(result2.exceptionOrNull()?.message, "동영상 삭제에 실패하였습니다.")
    }
}