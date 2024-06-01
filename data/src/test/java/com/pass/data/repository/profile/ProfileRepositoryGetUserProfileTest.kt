package com.pass.data.repository.profile

import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.FirebaseStorage
import com.pass.data.di.DateTimeProvider
import com.pass.data.repository.ProfileRepositoryImpl
import com.pass.data.util.CalculateUtil
import com.pass.data.util.FirebaseAuthUtil
import com.pass.data.util.FirebaseDatabaseUtil
import com.pass.data.util.FirebaseStorageUtil
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test

class ProfileRepositoryGetUserProfileTest {

    // firebase 모킹
    private val auth = mockk<FirebaseAuth>()
    private val fireStore = mockk<FirebaseFirestore>()
    private val storage = mockk<FirebaseStorage>()

    // util 선언
    private val firebaseAuthUtil = FirebaseAuthUtil(auth)
    private val firebaseDatabaseUtil = FirebaseDatabaseUtil(auth, fireStore)
    private val firebaseStorageUtil = FirebaseStorageUtil(storage)
    private val calculationUtil = CalculateUtil(DateTimeProvider())

    // fireStore 모킹
    private val mockFireStoreTask = mockk<Task<DocumentSnapshot>>()
    private val mockFireStoreException = RuntimeException("Error")
    private val mockDocumentSnapshot = mockk<DocumentSnapshot>()

    private val mockFireStoreQuerySnapshotTask = mockk<Task<QuerySnapshot>>()
    private val mockQuerySnapshot = mockk<QuerySnapshot>()

    // repository 초기화
    private val profileRepositoryImpl = ProfileRepositoryImpl(auth, firebaseAuthUtil, firebaseDatabaseUtil, firebaseStorageUtil, calculationUtil)

    @Test
    fun testSuccessGetUserProfile() = runBlocking {
        // fireStore getUserProfile 성공 가정
        every { auth.currentUser?.uid } returns "test123"
        every { mockFireStoreTask.isSuccessful } returns true
        every { mockFireStoreQuerySnapshotTask.isSuccessful } returns true
        every { mockQuerySnapshot.documents } returns listOf(mockDocumentSnapshot, mockDocumentSnapshot)
        every { mockDocumentSnapshot.getString(any()) } returns "20240601223830"
        every { mockDocumentSnapshot.id } returns "test_test"

        // listener 성공 가정
        every { mockFireStoreTask.addOnSuccessListener(any()) } answers {
            firstArg<OnSuccessListener<DocumentSnapshot>>().onSuccess(mockDocumentSnapshot)
            mockFireStoreTask
        }
        every { mockFireStoreTask.addOnFailureListener(any()) } answers {
            mockFireStoreTask
        }
        every { mockFireStoreQuerySnapshotTask.addOnSuccessListener(any()) } answers {
            firstArg<OnSuccessListener<QuerySnapshot>>().onSuccess(mockQuerySnapshot)
            mockFireStoreQuerySnapshotTask
        }
        every { mockFireStoreQuerySnapshotTask.addOnFailureListener(any()) } answers {
            mockFireStoreQuerySnapshotTask
        }

        // fireStore getUserProfile 성공 시나리오 테스트
        every { fireStore.collection(any()).document(any()).get() } returns mockFireStoreTask
        every { fireStore.collection(any()).document(any()).collection(any()).get() } returns mockFireStoreQuerySnapshotTask

        // fireStore getUserProfile 성공 시나리오 테스트
        val result = profileRepositoryImpl.getUserProfile().first()
        Assert.assertTrue(result.isSuccess)
    }

    @Test
    fun testFailGetUserProfile() = runBlocking {
        // fireStore getUserProfile 실패 가정
        every { auth.currentUser?.uid } returns "test123"
        every { mockFireStoreTask.isSuccessful } returns false
        every { mockFireStoreQuerySnapshotTask.isSuccessful } returns false
        every { mockQuerySnapshot.documents } returns listOf(mockDocumentSnapshot, mockDocumentSnapshot)
        every { mockDocumentSnapshot.getString(any()) } returns "20240601223830"
        every { mockDocumentSnapshot.id } returns "test_test"

        // listener 실패 가정
        every { mockFireStoreTask.addOnSuccessListener(any()) } answers {
            mockFireStoreTask
        }
        every { mockFireStoreTask.addOnFailureListener(any()) } answers {
            firstArg<OnFailureListener>().onFailure(mockFireStoreException)
            mockFireStoreTask
        }
        every { mockFireStoreQuerySnapshotTask.addOnSuccessListener(any()) } answers {
            mockFireStoreQuerySnapshotTask
        }
        every { mockFireStoreQuerySnapshotTask.addOnFailureListener(any()) } answers {
            firstArg<OnFailureListener>().onFailure(mockFireStoreException)
            mockFireStoreQuerySnapshotTask
        }

        // firebase collection  메서드를 호출에 대한 실패 모킹 적용
        every { fireStore.collection(any()).document(any()).get() } returns mockFireStoreTask
        every { fireStore.collection(any()).document(any()).collection(any()).get() } returns mockFireStoreQuerySnapshotTask

        // fireStore getUserProfile 실패 시나리오 테스트
        val result = profileRepositoryImpl.getUserProfile().first()
        Assert.assertTrue(result.isFailure)
        Assert.assertEquals(mockFireStoreException.message, result.exceptionOrNull()?.message)
    }

    @Test
    fun testFailGetUserProfileWithNullUid() = runBlocking {
        every { auth.currentUser?.uid } returns null

        // fireStore getUserProfile 실패 시나리오 테스트
        val result = profileRepositoryImpl.getUserProfile().first()
        Assert.assertTrue(result.isFailure)
        Assert.assertEquals("오류가 발생하였습니다. 다시 로그인을 진행해주세요.", result.exceptionOrNull()?.message)
    }

    @Test
    fun testFailGetUserProfileWithNullUser() = runBlocking {
        // user null 가정
        every { auth.currentUser?.uid } returns null

        // fireStore getUserProfile 실패 시나리오 테스트
        val result = profileRepositoryImpl.getUserProfile().first()
        Assert.assertTrue(result.isFailure)
        Assert.assertEquals("오류가 발생하였습니다. 다시 로그인을 진행해주세요.", result.exceptionOrNull()?.message)
    }

}