package com.pass.data.manager.database.firebase_database

import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.pass.data.manager.database.FirebaseDatabaseManagerImpl
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Assert.assertTrue
import org.junit.Test

class FirebaseDatabaseManagerImplReadDataListTest {

    // firebase 모킹
    private val mockFirebaseAuth = mockk<FirebaseAuth>()
    private val mockFirebaseFireStore = mockk<FirebaseFirestore>()

    // Task 객체 모킹
    private val mockTaskQuerySnapshot = mockk<Task<QuerySnapshot>>()
    private val mockQuerySnapshot = mockk<QuerySnapshot>()

    private val testCollectionPath = "videos"
    private val testDocumentPath = "test"
    private val testListDocumentSnapshot = emptyList<DocumentSnapshot>()

    private val firebaseDatabaseService = FirebaseDatabaseManagerImpl(mockFirebaseAuth, mockFirebaseFireStore)

    @Test
    fun testSuccessReadDataList() = runBlocking {
        every { mockTaskQuerySnapshot.result } returns mockQuerySnapshot
        every { mockQuerySnapshot.documents } returns testListDocumentSnapshot

        every { mockFirebaseFireStore.collection(any()).get() } answers { mockTaskQuerySnapshot }

        every { mockTaskQuerySnapshot.addOnSuccessListener(any()) } answers {
            firstArg<OnSuccessListener<QuerySnapshot>>().onSuccess(mockQuerySnapshot)
            mockTaskQuerySnapshot
        }

        every { mockTaskQuerySnapshot.addOnFailureListener(any()) } answers {
            mockTaskQuerySnapshot
        }

        val result = firebaseDatabaseService.readDataList(
            collectionPath = testCollectionPath
        ).first()

        assertTrue(result.isSuccess)
    }

    @Test
    fun testSuccessReadDataListWithDocumentPathAndCollection2() = runBlocking {
        every { mockTaskQuerySnapshot.result } returns mockQuerySnapshot
        every { mockQuerySnapshot.documents } returns testListDocumentSnapshot

        every { mockFirebaseFireStore.collection(any()).document(any()).collection(any()).get() } answers { mockTaskQuerySnapshot }

        every { mockTaskQuerySnapshot.addOnSuccessListener(any()) } answers {
            firstArg<OnSuccessListener<QuerySnapshot>>().onSuccess(mockQuerySnapshot)
            mockTaskQuerySnapshot
        }

        every { mockTaskQuerySnapshot.addOnFailureListener(any()) } answers {
            mockTaskQuerySnapshot
        }

        val result = firebaseDatabaseService.readDataList(
            collectionPath = testCollectionPath,
            documentPath = testDocumentPath,
            collectionPath2 = testCollectionPath
        ).first()

        assertTrue(result.isSuccess)
    }

    @Test
    fun testFailReadDataList() = runBlocking {
        val testException = Exception("test fail")

        every { mockFirebaseFireStore.collection(any()).get() } answers { mockTaskQuerySnapshot }

        every { mockTaskQuerySnapshot.addOnSuccessListener(any()) } answers {
            mockTaskQuerySnapshot
        }

        every { mockTaskQuerySnapshot.addOnFailureListener(any()) } answers {
            firstArg<OnFailureListener>().onFailure(testException)
            mockTaskQuerySnapshot
        }

        val result = firebaseDatabaseService.readDataList(
            collectionPath = testCollectionPath
        ).first()

        assertTrue(result.isFailure)
        Assert.assertEquals(result.exceptionOrNull()?.message, testException.message)
    }

    @Test
    fun testFailReadDataListWithDocumentAndCollection2() = runBlocking {
        val testException = Exception("test fail")

        every { mockFirebaseFireStore.collection(any()).document(any()).collection(any()).get() } answers { mockTaskQuerySnapshot }

        every { mockTaskQuerySnapshot.addOnSuccessListener(any()) } answers {
            mockTaskQuerySnapshot
        }

        every { mockTaskQuerySnapshot.addOnFailureListener(any()) } answers {
            firstArg<OnFailureListener>().onFailure(testException)
            mockTaskQuerySnapshot
        }

        val result = firebaseDatabaseService.readDataList(
            collectionPath = testCollectionPath,
            documentPath = testDocumentPath,
            collectionPath2 = testCollectionPath
        ).first()

        assertTrue(result.isFailure)
        Assert.assertEquals(result.exceptionOrNull()?.message, testException.message)
    }
}