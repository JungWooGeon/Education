package com.pass.data.util.firebase_storage

import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.FirebaseStorage
import com.pass.data.manager.database.FirebaseStorageManagerImpl
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test

class FirebaseStorageManagerImplDeleteFileTest {

    // firebase 모킹
    private val mockFirebaseStorage = mockk<FirebaseStorage>()

    // Task 객체 모킹
    private val mockTaskVoid = mockk<Task<Void>>()

    private val testPathString = "video_thumbnail/test1234_202406020224"

    private val firebaseStorageService = FirebaseStorageManagerImpl(mockFirebaseStorage)

    @Test
    fun testSuccessDeleteFile() = runBlocking {
        every { mockFirebaseStorage.reference.child(any()).delete() } answers { mockTaskVoid }

        every { mockTaskVoid.addOnSuccessListener(any()) } answers {
            firstArg<OnSuccessListener<Unit>>().onSuccess(null)
            mockTaskVoid
        }
        every { mockTaskVoid.addOnFailureListener(any()) } answers {
            mockTaskVoid
        }

        val result = firebaseStorageService.deleteFile(pathString = testPathString).first()
        assertTrue(result.isSuccess)
    }

    @Test
    fun testFailDeleteFile() = runBlocking {
        val testException = Exception("test exception")

        every { mockFirebaseStorage.reference.child(any()).delete() } answers { mockTaskVoid }

        every { mockTaskVoid.addOnSuccessListener(any()) } answers {
            mockTaskVoid
        }
        every { mockTaskVoid.addOnFailureListener(any()) } answers {
            firstArg<OnFailureListener>().onFailure(testException)
            mockTaskVoid
        }

        val result = firebaseStorageService.deleteFile(pathString = testPathString).first()
        assertTrue(result.isFailure)
    }
}