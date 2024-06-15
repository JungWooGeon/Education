package com.pass.data.manager.database.firebase_storage

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
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@RunWith(RobolectricTestRunner::class)
class FirebaseStorageManagerImplUpdateFileTest {

    // firebase 모킹
    private val mockFirebaseStorage = mockk<FirebaseStorage>()

    // Task 객체 모킹
    private val mockTaskUri = mockk<Task<Uri>>()
    private val mockUploadTask = mockk<UploadTask>()
    private val mockUploadTaskTaskSnapshot = mockk<UploadTask.TaskSnapshot>()
    private val mockMediaUtil = mockk<MediaUtil>()

    private val testFileUri = "test file uri"
    private val testPathString = "test path string"
    private val testUri = "testUri".toUri()

    private val firebaseStorageService = FirebaseStorageManagerImpl(mockFirebaseStorage, mockMediaUtil)

    @Before
    fun setup() {
        every { mockMediaUtil.urlDecode(any()) } answers {
            URLDecoder.decode(firstArg(), StandardCharsets.UTF_8.toString())
        }

        every { mockMediaUtil.urlEncode(any()) } answers {
            val inputUri = firstArg<Uri>()

            URLEncoder.encode(
                inputUri.toString(),
                StandardCharsets.UTF_8.toString()
            )
        }
    }

    @Test
    fun testSuccessUpdateFile() = runBlocking {
        every { mockUploadTaskTaskSnapshot.metadata?.reference?.downloadUrl } answers { mockTaskUri }
        every { mockFirebaseStorage.reference.child(any()).putFile(any()) } answers { mockUploadTask }

        every { mockTaskUri.addOnSuccessListener(any()) } answers {
            firstArg<OnSuccessListener<Uri>>().onSuccess(testUri)
            mockTaskUri
        }
        every { mockTaskUri.addOnFailureListener(any()) } answers {
            mockTaskUri
        }
        every { mockUploadTask.addOnSuccessListener(any()) } answers {
            firstArg<OnSuccessListener<UploadTask.TaskSnapshot>>().onSuccess(mockUploadTaskTaskSnapshot)
            mockUploadTask
        }
        every { mockUploadTask.addOnFailureListener(any()) } answers {
            mockUploadTask
        }

        val result = firebaseStorageService.updateFile(
            fileUri = testFileUri,
            pathString = testPathString
        ).first()

        assertTrue(result.isSuccess)
    }

    @Test
    fun testFailUpdateFile() = runBlocking {
        every { mockUploadTaskTaskSnapshot.metadata?.reference?.downloadUrl } answers { mockTaskUri }
        every { mockFirebaseStorage.reference.child(any()).putFile(any()) } answers { mockUploadTask }

        every { mockTaskUri.addOnSuccessListener(any()) } answers {
            firstArg<OnSuccessListener<Uri>>().onSuccess(testUri)
            mockTaskUri
        }
        every { mockTaskUri.addOnFailureListener(any()) } answers {
            mockTaskUri
        }
        every { mockUploadTask.addOnSuccessListener(any()) } answers {
            mockUploadTask
        }
        every { mockUploadTask.addOnFailureListener(any()) } answers {
            firstArg<OnFailureListener>().onFailure(Exception())
            mockUploadTask
        }

        val result = firebaseStorageService.updateFile(
            fileUri = testFileUri,
            pathString = testPathString
        ).first()

        assertTrue(result.isFailure)
        assertEquals(result.exceptionOrNull()?.message, "프로필 사진 업로드를 시도하던 중 오류가 발생하였습니다. 잠시 후 시도해주세요.")
    }

    @Test
    fun testFailUpdateFileWithUpdateProfileUrl() = runBlocking {
        every { mockUploadTaskTaskSnapshot.metadata?.reference?.downloadUrl } answers { mockTaskUri }
        every { mockFirebaseStorage.reference.child(any()).putFile(any()) } answers { mockUploadTask }

        every { mockTaskUri.addOnSuccessListener(any()) } answers {
            mockTaskUri
        }
        every { mockTaskUri.addOnFailureListener(any()) } answers {
            firstArg<OnFailureListener>().onFailure(Exception())
            mockTaskUri
        }
        every { mockUploadTask.addOnSuccessListener(any()) } answers {
            firstArg<OnSuccessListener<UploadTask.TaskSnapshot>>().onSuccess(mockUploadTaskTaskSnapshot)
            mockUploadTask
        }
        every { mockUploadTask.addOnFailureListener(any()) } answers {
            mockUploadTask
        }

        val result = firebaseStorageService.updateFile(
            fileUri = testFileUri,
            pathString = testPathString
        ).first()

        assertTrue(result.isFailure)
        assertEquals(result.exceptionOrNull()?.message, "프로필 사진 업로드를 시도하던 중 오류가 발생하였습니다. 잠시 후 시도해주세요.")
    }
}