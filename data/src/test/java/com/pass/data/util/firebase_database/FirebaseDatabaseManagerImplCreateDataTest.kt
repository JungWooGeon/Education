package com.pass.data.util.firebase_database

import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.pass.data.manager.database.FirebaseDatabaseManagerImpl
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test

class FirebaseDatabaseManagerImplCreateDataTest {

    // firebase 모킹
    private val mockFirebaseAuth = mockk<FirebaseAuth>()
    private val mockFirebaseFireStore = mockk<FirebaseFirestore>()

    // Task 객체 모킹
    private val mockTaskVoid = mockk<Task<Void>>()

    private val testCollectionPath = "videos"
    private val testDocumentPath = "test"
    private val testTitle = "test title"
    private val testUid = "test uid"
    private val testVideoThumbnailUrl = "test video thumbnail url"
    private val testVideoUrl = "test video url"
    private val testTime = "2024060200431212"

    private val firebaseDatabaseService = FirebaseDatabaseManagerImpl(mockFirebaseAuth, mockFirebaseFireStore)

    @Test
    fun testSuccessCreateData() = runBlocking {
        val testHashMap = hashMapOf(
            "title" to testTitle,
            "userId" to testUid,
            "videoThumbnailUrl" to testVideoThumbnailUrl,
            "videoUrl" to testVideoUrl,
            "time" to testTime
        )

        every { mockTaskVoid.isSuccessful } returns true
        every { mockFirebaseFireStore.collection(any()).document(any()).set(any()) } answers { mockTaskVoid }
        every { mockTaskVoid.addOnSuccessListener(any()) } answers {
            firstArg<OnSuccessListener<Void>>().onSuccess(null)
            mockTaskVoid
        }
        every { mockTaskVoid.addOnFailureListener(any()) } answers {
            mockTaskVoid
        }

        val result = firebaseDatabaseService.createData(
            dataMap = testHashMap,
            collectionPath = testCollectionPath,
            documentPath = testDocumentPath,
        ).first()

        Assert.assertTrue(result.isSuccess)
    }

    @Test
    fun testSuccessCreateDataWithCollection2AndDocument2() = runBlocking {
        val testHashMap = hashMapOf(
            "title" to testTitle,
            "userId" to testUid,
            "videoThumbnailUrl" to testVideoThumbnailUrl,
            "videoUrl" to testVideoUrl,
            "time" to testTime
        )

        every { mockTaskVoid.isSuccessful } returns true
        every { mockFirebaseFireStore.collection(any()).document(any()).collection(any()).document(any()).set(any()) } answers { mockTaskVoid }
        every { mockTaskVoid.addOnSuccessListener(any()) } answers {
            firstArg<OnSuccessListener<Void>>().onSuccess(null)
            mockTaskVoid
        }
        every { mockTaskVoid.addOnFailureListener(any()) } answers {
            mockTaskVoid
        }

        val result = firebaseDatabaseService.createData(
            dataMap = testHashMap,
            collectionPath = testCollectionPath,
            documentPath = testDocumentPath,
            collectionPath2 = testCollectionPath,
            documentPath2 = testDocumentPath
        ).first()

        Assert.assertTrue(result.isSuccess)
    }

    @Test
    fun testFailCreateData() = runBlocking {
        val testException = Exception("test fail")
        val testHashMap = hashMapOf(
            "title" to testTitle,
            "userId" to testUid,
            "videoThumbnailUrl" to testVideoThumbnailUrl,
            "videoUrl" to testVideoUrl,
            "time" to testTime
        )

        every { mockTaskVoid.exception } returns testException
        every { mockTaskVoid.isSuccessful } returns false
        every { mockFirebaseFireStore.collection(any()).document(any()).set(any()) } answers { mockTaskVoid }
        every { mockTaskVoid.addOnSuccessListener(any()) } answers {
            mockTaskVoid
        }
        every { mockTaskVoid.addOnFailureListener(any()) } answers {
            firstArg<OnFailureListener>().onFailure(testException)
            mockTaskVoid
        }

        val result = firebaseDatabaseService.createData(
            dataMap = testHashMap,
            collectionPath = testCollectionPath,
            documentPath = testDocumentPath
        ).first()

        Assert.assertTrue(result.isFailure)
        Assert.assertEquals(result.exceptionOrNull()?.message, testException.message)
    }

    @Test
    fun testFailCreateDataWithCollection2AndDocument2() = runBlocking {
        val testException = Exception("test fail")
        val testHashMap = hashMapOf(
            "title" to testTitle,
            "userId" to testUid,
            "videoThumbnailUrl" to testVideoThumbnailUrl,
            "videoUrl" to testVideoUrl,
            "time" to testTime
        )

        every { mockTaskVoid.exception } returns testException
        every { mockTaskVoid.isSuccessful } returns false
        every { mockFirebaseFireStore.collection(any()).document(any()).collection(any()).document(any()).set(any()) } answers { mockTaskVoid }
        every { mockTaskVoid.addOnSuccessListener(any()) } answers {
            mockTaskVoid
        }
        every { mockTaskVoid.addOnFailureListener(any()) } answers {
            firstArg<OnFailureListener>().onFailure(testException)
            mockTaskVoid
        }

        val result = firebaseDatabaseService.createData(
            dataMap = testHashMap,
            collectionPath = testCollectionPath,
            documentPath = testDocumentPath,
            collectionPath2 = testCollectionPath,
            documentPath2 = testDocumentPath
        ).first()

        Assert.assertTrue(result.isFailure)
        Assert.assertEquals(result.exceptionOrNull()?.message, testException.message)
    }
}