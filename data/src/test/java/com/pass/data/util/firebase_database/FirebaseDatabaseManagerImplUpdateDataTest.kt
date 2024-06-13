package com.pass.data.util.firebase_database

import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.pass.data.manager.database.FirebaseDatabaseManagerImpl
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FirebaseDatabaseManagerImplUpdateDataTest {

    // firebase 모킹
    private val mockFirebaseAuth = mockk<FirebaseAuth>()
    private val mockFirebaseUser = mockk<FirebaseUser>()
    private val mockFirebaseFireStore = mockk<FirebaseFirestore>()

    // Task 객체 모킹
    private val mockTaskAny = mockk<Task<Any>>()

    private val testCollectionPath = "videos"
    private val testName = "test name"
    private val testField = "test field"

    private val firebaseDatabaseService = FirebaseDatabaseManagerImpl(mockFirebaseAuth, mockFirebaseFireStore)

    @Test
    fun testSuccessUpdateData() = runBlocking {
        every { mockFirebaseAuth.currentUser } returns mockFirebaseUser
        every { mockFirebaseUser.uid } returns "test1234"

        every { mockFirebaseFireStore.runTransaction<Any>(any()) }answers { mockTaskAny }

        every { mockTaskAny.addOnSuccessListener(any()) } answers {
            firstArg<OnSuccessListener<Void>>().onSuccess(null)
            mockTaskAny
        }

        every { mockTaskAny.addOnFailureListener(any()) } answers {
            mockTaskAny
        }

        val result = firebaseDatabaseService.updateData(
            name = testName,
            field = testField,
            collectionPath = testCollectionPath
        ).first()

        assertTrue(result.isSuccess)
    }

    @Test
    fun testFailUpdateData() = runBlocking {
        val testException = Exception("test fail")

        every { mockFirebaseAuth.currentUser } returns mockFirebaseUser
        every { mockFirebaseUser.uid } returns "test1234"

        every { mockFirebaseFireStore.runTransaction<Any>(any()) }answers { mockTaskAny }

        every { mockTaskAny.addOnSuccessListener(any()) } answers {

            mockTaskAny
        }

        every { mockTaskAny.addOnFailureListener(any()) } answers {
            firstArg<OnFailureListener>().onFailure(testException)
            mockTaskAny
        }

        val result = firebaseDatabaseService.updateData(
            name = testName,
            field = testField,
            collectionPath = testCollectionPath
        ).first()

        assertTrue(result.isFailure)
        assertEquals(result.exceptionOrNull()?.message, testException.message)
    }

    @Test
    fun testFailUpdateDataWithUserIdNull() = runBlocking {
        every { mockFirebaseAuth.currentUser } returns null

        val result = firebaseDatabaseService.updateData(
            name = testName,
            field = testField,
            collectionPath = testCollectionPath
        ).first()

        assertTrue(result.isFailure)
        assertEquals(result.exceptionOrNull()?.message, "오류가 발생하였습니다. 다시 로그인을 진행해주세요.")
    }
}