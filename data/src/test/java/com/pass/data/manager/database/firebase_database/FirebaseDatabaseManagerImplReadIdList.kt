package com.pass.data.manager.database.firebase_database

import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
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

class FirebaseDatabaseManagerImplReadIdList {

    // firebase 모킹
    private val mockFirebaseAuth = mockk<FirebaseAuth>()
    private val mockFirebaseFireStore = mockk<FirebaseFirestore>()

    // Task 객체 모킹
    private val mockTaskQuerySnapshot = mockk<Task<QuerySnapshot>>()
    private val mockQuerySnapshot = mockk<QuerySnapshot>()

    private val testListDocumentSnapshot = emptyList<DocumentSnapshot>()
    private val testIdList = listOf("test1234", "test111")

    private val firebaseDatabaseService = FirebaseDatabaseManagerImpl(mockFirebaseAuth, mockFirebaseFireStore)

    @Test
    fun testSuccessReadIdList() = runBlocking {
        every { mockTaskQuerySnapshot.result } returns mockQuerySnapshot
        every { mockQuerySnapshot.documents } returns testListDocumentSnapshot

        every { mockFirebaseFireStore.collection(any()).whereIn(FieldPath.documentId(), any()).get() } answers { mockTaskQuerySnapshot }

        every { mockTaskQuerySnapshot.addOnSuccessListener(any()) } answers {
            firstArg<OnSuccessListener<QuerySnapshot>>().onSuccess(mockQuerySnapshot)
            mockTaskQuerySnapshot
        }

        every { mockTaskQuerySnapshot.addOnFailureListener(any()) } answers {
            mockTaskQuerySnapshot
        }

        val result = firebaseDatabaseService.readIdList(userIdList = testIdList).first()

        assertTrue(result.isSuccess)
    }

    @Test
    fun testFailReadIdList() = runBlocking {
        val testException = Exception("test fail")

        every { mockTaskQuerySnapshot.result } returns mockQuerySnapshot
        every { mockQuerySnapshot.documents } returns testListDocumentSnapshot

        every { mockFirebaseFireStore.collection(any()).whereIn(FieldPath.documentId(), any()).get() } answers { mockTaskQuerySnapshot }

        every { mockTaskQuerySnapshot.addOnSuccessListener(any()) } answers {
            mockTaskQuerySnapshot
        }

        every { mockTaskQuerySnapshot.addOnFailureListener(any()) } answers {
            firstArg<OnFailureListener>().onFailure(testException)
            mockTaskQuerySnapshot
        }

        val result = firebaseDatabaseService.readIdList(userIdList = testIdList).first()

        assertTrue(result.isFailure)
        Assert.assertEquals(result.exceptionOrNull()?.message, testException.message)
    }

    @Test
    fun testFailReadIdListWithEmptyUserIdList() = runBlocking {
        val result = firebaseDatabaseService.readIdList(userIdList = emptyList()).first()

        assertTrue(result.isSuccess)

        val resultList = result.getOrNull()
        assertTrue(resultList != null && resultList.isEmpty())
    }
}