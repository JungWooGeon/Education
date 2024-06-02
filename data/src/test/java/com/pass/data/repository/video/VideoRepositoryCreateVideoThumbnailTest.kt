package com.pass.data.repository.video

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import androidx.core.net.toUri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.pass.data.di.DateTimeProvider
import com.pass.data.repository.VideoRepositoryImpl
import com.pass.data.util.CalculateUtil
import com.pass.domain.util.DatabaseUtil
import com.pass.domain.util.StorageUtil
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.ByteArrayOutputStream

@RunWith(RobolectricTestRunner::class)
class VideoRepositoryCreateVideoThumbnailTest {

    private val mockFirebaseAuth = mockk<FirebaseAuth>()
    private val mockkDatabaseUtil = mockk<DatabaseUtil<DocumentSnapshot>>()
    private val mockStorageUtil = mockk<StorageUtil>()
    private val mockCalculateUtil = mockk<CalculateUtil>()
    private val mockContext = mockk<Context>()
    private val mockMediaMetadataRetriever = mockk<MediaMetadataRetriever>()
    private val mockByteArrayOutputStream = mockk<ByteArrayOutputStream>()
    private val mockBitmap = mockk<Bitmap>()
    private val mockByteArray = ByteArray(10) { it.toByte() }

    private val testVideoUri = "testUri"
    private val testUri = testVideoUri.toUri()

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
    fun testSuccessCreateVideoThumbnail() {
        every { mockMediaMetadataRetriever.setDataSource(mockContext, testUri) } just runs
        every { mockMediaMetadataRetriever.frameAtTime } returns mockBitmap
        every { mockMediaMetadataRetriever.release() } just runs
        every { mockBitmap.compress(any(), any(), any()) } returns true
        every { mockByteArrayOutputStream.toByteArray() } returns mockByteArray

        val result = videoRepositoryImpl.createVideoThumbnail(videoUri = testVideoUri)
        assertTrue(result.isSuccess)
    }

    @Test
    fun testFailCreateVideoThumbnailWithThumbnailBitmapNull() {
        every { mockMediaMetadataRetriever.setDataSource(mockContext, testUri) } just runs
        every { mockMediaMetadataRetriever.frameAtTime } returns null
        every { mockMediaMetadataRetriever.release() } just runs
        every { mockBitmap.compress(any(), any(), any()) } returns true
        every { mockByteArrayOutputStream.toByteArray() } returns mockByteArray

        val result = videoRepositoryImpl.createVideoThumbnail(videoUri = testVideoUri)
        assertTrue(result.isFailure)
        assertEquals(result.exceptionOrNull()?.message, "동영상 선택에 실패하였습니다.")
    }

    @Test
    fun testFailCreateVideoThumbnailWithThrowException() {
        val testException = IllegalArgumentException("test exception")

        every { mockMediaMetadataRetriever.setDataSource(mockContext, testUri) } throws testException
        every { mockMediaMetadataRetriever.frameAtTime } returns mockBitmap
        every { mockMediaMetadataRetriever.release() } just runs
        every { mockBitmap.compress(any(), any(), any()) } returns true
        every { mockByteArrayOutputStream.toByteArray() } returns mockByteArray

        val result = videoRepositoryImpl.createVideoThumbnail(videoUri = testVideoUri)
        assertTrue(result.isFailure)
        assertEquals(result.exceptionOrNull()?.message, testException.message)
    }
}