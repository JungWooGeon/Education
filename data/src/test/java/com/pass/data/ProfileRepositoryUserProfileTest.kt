package com.pass.data

import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.pass.data.repository.ProfileRepositoryImpl
import com.pass.domain.model.Profile
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProfileRepositoryUserProfileTest {
    // firebase 모킹
    private val auth = mockk<FirebaseAuth>()
    private val fireStore = mockk<FirebaseFirestore>()

    // fireStore 모킹
    private val mockFireStoreTask = mockk<Task<DocumentSnapshot>>()
    private val mockFireStoreException = RuntimeException("Error")
    private val mockDocumentSnapshot = mockk<DocumentSnapshot>()

    private val mockFireStoreTransactionTask = mockk<Task<Any>>()

    private val testProfile = Profile("test name", "")

    // repository 초기화
    private val profileRepositoryImpl = ProfileRepositoryImpl(auth, fireStore)

    @Test
    fun testSuccessGetUserProfile() = runBlocking {
        // fireStore getUserProfile 성공 가정
        every { auth.currentUser?.uid } returns "test123"
        every { mockDocumentSnapshot.toObject(Profile::class.java) } returns testProfile
        every { mockFireStoreTask.isSuccessful } returns true

        // listener 성공 가정
        every { mockFireStoreTask.addOnSuccessListener(any()) } answers {
            firstArg<OnSuccessListener<DocumentSnapshot>>().onSuccess(mockDocumentSnapshot)
            mockFireStoreTask
        }
        every { mockFireStoreTask.addOnFailureListener(any()) } answers {
            mockFireStoreTask
        }

        // fireStore getUserProfile 성공 시나리오 테스트
        every { fireStore.collection(any()).document(any()).get() } returns mockFireStoreTask

        // fireStore getUserProfile 성공 시나리오 테스트
        val result = profileRepositoryImpl.getUserProfile().first()
        assertTrue(result.isSuccess)
    }

    @Test
    fun testFailGetUserProfile() = runBlocking {
        // fireStore getUserProfile 실패 가정
        every { auth.currentUser?.uid } returns "test123"
        every { mockFireStoreTask.isSuccessful } returns false

        // listener 실패 가정
        every { mockFireStoreTask.addOnSuccessListener(any()) } answers {
            mockFireStoreTask
        }
        every { mockFireStoreTask.addOnFailureListener(any()) } answers {
            firstArg<OnFailureListener>().onFailure(mockFireStoreException)
            mockFireStoreTask
        }

        // firebase collection  메서드를 호출에 대한 실패 모킹 적용
        every { fireStore.collection(any()).document(any()).get() } returns mockFireStoreTask

        // fireStore getUserProfile 실패 시나리오 테스트
        val result = profileRepositoryImpl.getUserProfile().first()
        assertTrue(result.isFailure)
        assertEquals(mockFireStoreException.message, result.exceptionOrNull()?.message)
    }

    @Test
    fun testFailGetUserProfileWithNullUid() = runBlocking {
        every { auth.currentUser?.uid } returns null

        // fireStore getUserProfile 실패 시나리오 테스트
        val result = profileRepositoryImpl.getUserProfile().first()
        assertTrue(result.isFailure)
        assertEquals("오류가 발생하였습니다. 다시 로그인을 진행해주세요.", result.exceptionOrNull()?.message)
    }

    @Test
    fun testFailGetUserProfileWithNullUser() = runBlocking {
        // fireStore getUserProfile 성공 + user null 가정
        every { auth.currentUser?.uid } returns "test123"
        every { mockDocumentSnapshot.toObject(Profile::class.java) } returns null
        every { mockFireStoreTask.isSuccessful } returns true

        // listener 성공 가정
        every { mockFireStoreTask.addOnSuccessListener(any()) } answers {
            firstArg<OnSuccessListener<DocumentSnapshot>>().onSuccess(mockDocumentSnapshot)
            mockFireStoreTask
        }
        every { mockFireStoreTask.addOnFailureListener(any()) } answers {
            mockFireStoreTask
        }

        // firebase collection  메서드를 호출에 대한 실패 모킹 적용
        every { fireStore.collection(any()).document(any()).get() } returns mockFireStoreTask

        // fireStore getUserProfile 실패 시나리오 테스트
        val result = profileRepositoryImpl.getUserProfile().first()
        assertTrue(result.isFailure)
        assertEquals("프로필을 조회할 수 없습니다.", result.exceptionOrNull()?.message)
    }

    @Test
    fun testSuccessUpdateUserProfileName(): Unit = runBlocking {
        // fireStore updateUserProfile 성공 가정
        every { auth.currentUser?.uid } returns "test123"
        every { mockFireStoreTransactionTask.isSuccessful } returns true

        // listener 성공 가정
        every { mockFireStoreTransactionTask.addOnSuccessListener(any()) } answers {
            firstArg<OnSuccessListener<Unit>>().onSuccess(null)
            mockFireStoreTransactionTask
        }
        every { mockFireStoreTransactionTask.addOnFailureListener(any()) } answers {
            mockFireStoreTransactionTask
        }

        // firestore runtransaction 호출에 대한 메소드 모킹 적용
        every { fireStore.runTransaction<Any>(any()) } answers {
            mockFireStoreTransactionTask
        }

        // fireStore updateUserProfile 성공 시나리오 테스트
        val result = profileRepositoryImpl.updateUserProfileName("test").first()
        assertTrue(result.isSuccess)
    }

    @Test
    fun testFailUpdateUserProfile() = runBlocking {
        // fireStore updateUserProfile 실패 가정
        every { auth.currentUser?.uid } returns "test123"
        every { mockFireStoreTransactionTask.isSuccessful } returns false

        // listener 실패 가정
        every { mockFireStoreTransactionTask.addOnSuccessListener(any()) } answers {
            mockFireStoreTransactionTask
        }
        every { mockFireStoreTransactionTask.addOnFailureListener(any()) } answers {
            firstArg<OnFailureListener>().onFailure(mockFireStoreException)
            mockFireStoreTransactionTask
        }

        // firestore runtransaction 호출에 대한 메소드 모킹 적용
        every { fireStore.runTransaction<Any>(any()) } answers {
            mockFireStoreTransactionTask
        }

        // fireStore updateUserProfile 실패 시나리오 테스트
        val result = profileRepositoryImpl.updateUserProfileName("test").first()
        assertTrue(result.isFailure)
        assertEquals(mockFireStoreException.message, result.exceptionOrNull()?.message)
    }

    @Test
    fun testFailUpdateUserProfileNameWithNullUid() = runBlocking {
        every { auth.currentUser?.uid } returns null

        val result = profileRepositoryImpl.updateUserProfileName("test").first()
        assertTrue(result.isFailure)
        assertEquals("오류가 발생하였습니다. 다시 로그인을 진행해주세요.", result.exceptionOrNull()?.message)
    }
}