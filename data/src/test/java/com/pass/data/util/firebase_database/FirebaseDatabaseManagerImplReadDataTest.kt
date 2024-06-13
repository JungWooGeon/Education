package com.pass.data.util.firebase_database

import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.pass.data.manager.database.FirebaseDatabaseManagerImpl
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FirebaseDatabaseManagerImplReadDataTest {

    // firebase 모킹
    private val mockFirebaseAuth = mockk<FirebaseAuth>()
    private val mockFirebaseFireStore = mockk<FirebaseFirestore>()

    // Task 객체 모킹
    private val mockTaskDocumentSnapshot = mockk<Task<DocumentSnapshot>>()

    private val testCollectionPath = "videos"
    private val testDocumentPath = "test"

    private val firebaseDatabaseService = FirebaseDatabaseManagerImpl(mockFirebaseAuth, mockFirebaseFireStore)

    @Test
    fun testSuccessReadData() = runBlocking {
        every { mockFirebaseFireStore.collection(any()).document(any()).get() } answers { mockTaskDocumentSnapshot }

        every { mockTaskDocumentSnapshot.addOnSuccessListener(any()) } answers {
            firstArg<OnSuccessListener<Void>>().onSuccess(null)
            mockTaskDocumentSnapshot
        }

        every { mockTaskDocumentSnapshot.addOnFailureListener(any()) } answers {
            mockTaskDocumentSnapshot
        }

        val result = firebaseDatabaseService.readData(
            collectionPath = testCollectionPath,
            documentPath = testDocumentPath
        ).first()

        assertTrue(result.isSuccess)
    }

    @Test
    fun testFailReadData() = runBlocking {
        val testException = Exception("test fail")

        every { mockFirebaseFireStore.collection(any()).document(any()).get() } answers { mockTaskDocumentSnapshot }

        every { mockTaskDocumentSnapshot.addOnSuccessListener(any()) } answers {
            mockTaskDocumentSnapshot
        }

        every { mockTaskDocumentSnapshot.addOnFailureListener(any()) } answers {
            firstArg<OnFailureListener>().onFailure(testException)
            mockTaskDocumentSnapshot
        }

        val result = firebaseDatabaseService.readData(
            collectionPath = testCollectionPath,
            documentPath = testDocumentPath
        ).first()

        assertTrue(result.isFailure)
        assertEquals(result.exceptionOrNull()?.message, testException.message)
    }
}