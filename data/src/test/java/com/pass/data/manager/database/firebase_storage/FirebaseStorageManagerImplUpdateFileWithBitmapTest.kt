package com.pass.data.manager.database.firebase_storage

import android.graphics.Bitmap
import android.net.Uri
import androidx.core.net.toUri
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.pass.data.manager.database.FirebaseStorageManagerImpl
import com.pass.data.util.MediaUtil
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@RunWith(RobolectricTestRunner::class)
class FirebaseStorageManagerImplUpdateFileWithBitmapTest {

    // firebase 모킹
    private val mockFirebaseStorage = mockk<FirebaseStorage>()
    private val mockMediaUtil = mockk<MediaUtil>()
    private val mockBitmap = mockk<Bitmap>()

    // Task 객체 모킹
    private val mockTaskUri = mockk<Task<Uri>>()
    private val mockUploadTask = mockk<UploadTask>()
    private val mockUploadTaskTaskSnapshot = mockk<UploadTask.TaskSnapshot>()

    private val testUri = "testUri".toUri()
    private val testVideoId = "testVideoId"
    private val testByteArray = byteArrayOf()

    private val firebaseStorageService = FirebaseStorageManagerImpl(mockFirebaseStorage, mockMediaUtil)

    @Before
    fun setup() {
        every { mockMediaUtil.convertStringToBitmap(any()) } returns mockBitmap
        every { mockMediaUtil.convertBitmapToByteArray(any()) } returns testByteArray
        every { mockMediaUtil.urlEncode(any()) } answers {
            val inputUri = firstArg<Uri>()

            URLEncoder.encode(
                inputUri.toString(),
                StandardCharsets.UTF_8.toString()
            )
        }
    }

    @Test
    fun testSuccessUpdateFileWithBitmap() = runBlocking {
        every { mockUploadTaskTaskSnapshot.metadata?.reference?.downloadUrl } answers { mockTaskUri }
        every { mockFirebaseStorage.reference.child(any()).putBytes(any()) } answers { mockUploadTask }

        every { mockUploadTask.addOnSuccessListener(any()) } answers {
            firstArg<OnSuccessListener<UploadTask.TaskSnapshot>>().onSuccess(mockUploadTaskTaskSnapshot)
            mockUploadTask
        }
        every { mockUploadTask.addOnFailureListener(any()) } answers {
            mockUploadTask
        }
        every { mockTaskUri.addOnSuccessListener(any()) } answers {
            firstArg<OnSuccessListener<Uri>>().onSuccess(testUri)
            mockTaskUri
        }
        every { mockTaskUri.addOnFailureListener(any()) } answers {
            mockTaskUri
        }

        val result = firebaseStorageService.updateFileWithBitmap(
            bitmap = mockBitmap,
            pathString = testVideoId
        ).first()

        assertTrue(result.isSuccess)
    }

    @Test
    fun testFailUpdateFileWithBitmap() = runBlocking {
        every { mockUploadTaskTaskSnapshot.metadata?.reference?.downloadUrl } answers { mockTaskUri }
        every { mockFirebaseStorage.reference.child(any()).putBytes(any()) } answers { mockUploadTask }

        every { mockUploadTask.addOnSuccessListener(any()) } answers {
            mockUploadTask
        }
        every { mockUploadTask.addOnFailureListener(any()) } answers {
            firstArg<OnFailureListener>().onFailure(Exception())
            mockUploadTask
        }
        every { mockTaskUri.addOnSuccessListener(any()) } answers {
            firstArg<OnSuccessListener<Uri>>().onSuccess(testUri)
            mockTaskUri
        }
        every { mockTaskUri.addOnFailureListener(any()) } answers {
            mockTaskUri
        }

        val result = firebaseStorageService.updateFileWithBitmap(
            bitmap = mockBitmap,
            pathString = testVideoId
        ).first()

        assertTrue(result.isFailure)
        assertEquals(result.exceptionOrNull()?.message, "업로드를 시도하던 중 오류가 발생하였습니다. 잠시 후 시도해주세요.")
    }

    @Test
    fun testFailUpdateFileWithBitmapWithUpdateProfileUrl() = runBlocking {
        every { mockUploadTaskTaskSnapshot.metadata?.reference?.downloadUrl } answers { mockTaskUri }
        every { mockFirebaseStorage.reference.child(any()).putBytes(any()) } answers { mockUploadTask }

        every { mockUploadTask.addOnSuccessListener(any()) } answers {
            firstArg<OnSuccessListener<UploadTask.TaskSnapshot>>().onSuccess(mockUploadTaskTaskSnapshot)
            mockUploadTask
        }
        every { mockUploadTask.addOnFailureListener(any()) } answers {
            mockUploadTask
        }
        every { mockTaskUri.addOnSuccessListener(any()) } answers {
            mockTaskUri
        }
        every { mockTaskUri.addOnFailureListener(any()) } answers {
            firstArg<OnFailureListener>().onFailure(Exception())
            mockTaskUri
        }

        val result = firebaseStorageService.updateFileWithBitmap(
            bitmap = mockBitmap,
            pathString = testVideoId
        ).first()

        assertTrue(result.isFailure)
        assertEquals(result.exceptionOrNull()?.message, "업로드를 시도하던 중 오류가 발생하였습니다. 잠시 후 시도해주세요.")
    }
}