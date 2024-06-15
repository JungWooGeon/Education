package com.pass.data.manager.database.firebase_database

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

class FirebaseDatabaseManagerImplDeleteDataTest {

    // firebase 모킹
    private val mockFirebaseAuth = mockk<FirebaseAuth>()
    private val mockFirebaseFireStore = mockk<FirebaseFirestore>()

    // Task 객체 모킹
    private val mockTaskVoid = mockk<Task<Void>>()

    private val testCollectionPath = "videos"
    private val testDocumentPath = "test"

    private val firebaseDatabaseService = FirebaseDatabaseManagerImpl(mockFirebaseAuth, mockFirebaseFireStore)

    @Test
    fun testSuccessDeleteData() = runBlocking {
        every { mockTaskVoid.isSuccessful } returns true
        every { mockFirebaseFireStore.collection(any()).document(any()).delete() } answers { mockTaskVoid }

        every { mockTaskVoid.addOnSuccessListener(any()) } answers {
            firstArg<OnSuccessListener<Void>>().onSuccess(null)
            mockTaskVoid
        }

        every { mockTaskVoid.addOnFailureListener(any()) } answers {
            mockTaskVoid
        }

        val result = firebaseDatabaseService.deleteData(
            collectionPath = testCollectionPath,
            documentPath = testDocumentPath
        ).first()

        Assert.assertTrue(result.isSuccess)
    }

    @Test
    fun testSuccessDeleteDataWithCollection2AndDocument2() = runBlocking {
        every { mockTaskVoid.isSuccessful } returns true
        every { mockFirebaseFireStore.collection(any()).document(any()).collection(any()).document(any()).delete() } answers { mockTaskVoid }

        every { mockTaskVoid.addOnSuccessListener(any()) } answers {
            firstArg<OnSuccessListener<Void>>().onSuccess(null)
            mockTaskVoid
        }

        every { mockTaskVoid.addOnFailureListener(any()) } answers {
            mockTaskVoid
        }

        val result = firebaseDatabaseService.deleteData(
            collectionPath = testCollectionPath,
            documentPath = testDocumentPath,
            collectionPath2 = testCollectionPath,
            documentPath2 = testDocumentPath
        ).first()

        Assert.assertTrue(result.isSuccess)
    }

    @Test
    fun testFailDeleteData() = runBlocking {
        val testException = Exception("test fail")

        every { mockTaskVoid.isSuccessful } returns false
        every { mockFirebaseFireStore.collection(any()).document(any()).delete() } answers { mockTaskVoid }

        every { mockTaskVoid.addOnSuccessListener(any()) } answers {
            mockTaskVoid
        }

        every { mockTaskVoid.addOnFailureListener(any()) } answers {
            firstArg<OnFailureListener>().onFailure(testException)
            mockTaskVoid
        }

        val result = firebaseDatabaseService.deleteData(
            collectionPath = testCollectionPath,
            documentPath = testDocumentPath
        ).first()

        Assert.assertTrue(result.isFailure)
        Assert.assertEquals(result.exceptionOrNull()?.message, testException.message)
    }

    @Test
    fun testFailDeleteDataWithCollection2AndDocument2() = runBlocking {
        val testException = Exception("test fail")

        every { mockTaskVoid.isSuccessful } returns false
        every { mockFirebaseFireStore.collection(any()).document(any()).collection(any()).document(any()).delete() } answers { mockTaskVoid }

        every { mockTaskVoid.addOnSuccessListener(any()) } answers {
            mockTaskVoid
        }

        every { mockTaskVoid.addOnFailureListener(any()) } answers {
            firstArg<OnFailureListener>().onFailure(testException)
            mockTaskVoid
        }

        val result = firebaseDatabaseService.deleteData(
            collectionPath = testCollectionPath,
            documentPath = testDocumentPath,
            collectionPath2 = testCollectionPath,
            documentPath2 = testDocumentPath
        ).first()

        Assert.assertTrue(result.isFailure)
        Assert.assertEquals(result.exceptionOrNull()?.message, testException.message)
    }
}