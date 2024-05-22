package com.pass.data

import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.pass.data.repository.ProfileRepositoryImpl
import com.pass.data.util.FirebaseAuthUtil
import com.pass.data.util.FirebaseDatabaseUtil
import com.pass.data.util.FirebaseStorageUtil
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ProfileRepositorySignUpTest {

    // firebase 모킹
    private val auth = mockk<FirebaseAuth>()
    private val fireStore = mockk<FirebaseFirestore>()
    private val storage = mockk<FirebaseStorage>()

    // util 선언
    private val firebaseAuthUtil = FirebaseAuthUtil(auth)
    private val firebaseDatabaseUtil = FirebaseDatabaseUtil(auth, fireStore)
    private val firebaseStorageUtil = FirebaseStorageUtil(auth, storage)

    // Task 객체 모킹
    private val mockException = Exception("signIn failed")
    private val mockTask = mockk<Task<AuthResult>>()
    private val mockAuthResult = mockk<AuthResult>()

    // fireStore 모킹
    private val mockFireStoreTask= mockk<Task<Void> >()
    private val mockFireStoreException = RuntimeException("Error")

    // repository 초기화
    private val profileRepositoryImpl = ProfileRepositoryImpl(auth, firebaseAuthUtil, firebaseDatabaseUtil, firebaseStorageUtil)

    @Before
    fun setup() {
        every { auth.signOut() } just runs
        every { auth.currentUser?.delete() } answers { null }
    }

    @Test
    fun testSuccessSignUp() = runBlocking {
        // 성공 시나리오 모킹
        every { mockTask.isSuccessful } returns true
        every { mockTask.result } returns mockAuthResult

        // addOnCompleteListener 메서드 호출에 대한 모킹
        every { mockTask.addOnCompleteListener(any()) } answers {
            firstArg<OnCompleteListener<AuthResult>>().onComplete(mockTask)
            mockTask
        }

        // fireStore create Task 테스트 성공 가정
        every { mockFireStoreTask.isSuccessful } returns true
        every { mockFireStoreTask.addOnSuccessListener(any()) } answers {
            firstArg<OnSuccessListener<Void>>().onSuccess(null)
            mockFireStoreTask
        }
        every { mockFireStoreTask.addOnFailureListener(any()) } answers {
            mockFireStoreTask
        }

        // firestore collection 호출에 대한 메소드 모킹 적용
        every { fireStore.collection(any()).document(any()).set(any()) } returns mockFireStoreTask

        // firebase createUserWithEmailAndPassword 메서드를 호출에 대한 성공 모킹
        every { auth.createUserWithEmailAndPassword(any(), any()) } returns mockTask
        every { auth.currentUser?.uid } returns "testUid"

        // signUp 성공 시나리오 테스트
        val result = profileRepositoryImpl.signUp("test@example.com", "password", "password").first()
        assertTrue(result.isSuccess)
    }

    @Test
    fun testFailSignUp() = runBlocking {
        // 실패 시나리오 모킹
        every { mockTask.isSuccessful } returns false
        every { mockTask.result } returns mockAuthResult
        every { mockTask.exception } returns mockException

        // addOnCompleteListener 메서드 호출에 대한 모킹
        every { mockTask.addOnCompleteListener(any()) } answers {
            firstArg<OnCompleteListener<AuthResult>>().onComplete(mockTask)
            mockTask
        }

        // firebase createUserWithEmailAndPassword 메서드를 호출에 대한 실패 모킹
        every { auth.createUserWithEmailAndPassword(any(), any()) } returns mockTask

        // signUp 실패 시나리오 테스트
        val result = profileRepositoryImpl.signUp("test@example.com", "password", "password").first()
        assertTrue(result.isFailure)
    }

    @Test
    fun testFailSignUpWithEmptyIdOrPassword() = runBlocking {
        // id or password or verifyPassword 가 빈 값일 때 false 값과 예외 메시지를 반환하는지 테스트
        var result = profileRepositoryImpl.signUp("", "password", "password").first()
        assertTrue(result.isFailure)
        assertEquals("아이디와 비밀번호를 입력해주세요.", result.exceptionOrNull()?.message)

        result = profileRepositoryImpl.signUp("test@example.com", "", "password").first()
        assertTrue(result.isFailure)
        assertEquals("아이디와 비밀번호를 입력해주세요.", result.exceptionOrNull()?.message)

        result = profileRepositoryImpl.signUp("test@example.com", "password", "").first()
        assertTrue(result.isFailure)
        assertEquals("아이디와 비밀번호를 입력해주세요.", result.exceptionOrNull()?.message)
    }

    @Test
    fun testFailSignUpWithEmptyOrNotEqualVerifyPassword() = runBlocking {
        // signUp 실패 시나리오 테스트 - 비밀번호 확인 불일치
        val result = profileRepositoryImpl.signUp("test@example.com", "password", "poss").first()
        assertTrue(result.isFailure)
        assertEquals("비밀번호가 맞지 않습니다.", result.exceptionOrNull()?.message)
    }

    @Test
    fun testFailSignUpWithFailCreateUserProfile() = runBlocking {
        // 성공 시나리오 모킹 - 처음 로직은 성공, 내부 Create DB 과정에서 실패
        every { mockTask.isSuccessful } returns true
        every { mockTask.result } returns mockAuthResult

        // addOnCompleteListener 메서드 호출에 대한 모킹
        every { mockTask.addOnCompleteListener(any()) } answers {
            firstArg<OnCompleteListener<AuthResult>>().onComplete(mockTask)
            mockTask
        }

        // fireStore create Task 테스트 실패 가정
        every { mockFireStoreTask.isSuccessful } returns false
        every { mockFireStoreTask.exception } returns mockFireStoreException
        every { mockFireStoreTask.addOnSuccessListener(any()) } answers {
            mockFireStoreTask
        }
        every { mockFireStoreTask.addOnFailureListener(any()) } answers {
            firstArg<OnFailureListener>().onFailure(mockFireStoreException)
            mockFireStoreTask
        }

        // firestore collection 호출에 대한 메소드 모킹 적용
        every { fireStore.collection(any()).document(any()).set(any()) } returns mockFireStoreTask

        // firebase createUserWithEmailAndPassword 메서드를 호출에 대한 실패 모킹 적용
        every { auth.createUserWithEmailAndPassword(any(), any()) } returns mockTask
        every { auth.currentUser?.uid } returns "testUid"

        // signUp 실패 시나리오 테스트
        val result = profileRepositoryImpl.signUp("test@example.com", "password", "password").first()
        assertTrue(result.isFailure)
        assertEquals(mockFireStoreException.message, result.exceptionOrNull()?.message)
    }

    @Test
    fun testFailSignUpWithUidNull() = runBlocking {
        // 성공 시나리오 모킹 - 처음 로직은 성공, 내부 uid null 로 인한 실패
        every { mockTask.isSuccessful } returns true
        every { mockTask.result } returns mockAuthResult

        // addOnCompleteListener 메서드 호출에 대한 모킹
        every { mockTask.addOnCompleteListener(any()) } answers {
            firstArg<OnCompleteListener<AuthResult>>().onComplete(mockTask)
            mockTask
        }

        // firebase createUserWithEmailAndPassword 메서드를 호출에 대한 실패 모킹 적용
        every { auth.createUserWithEmailAndPassword(any(), any()) } returns mockTask
        every { auth.currentUser?.uid } returns null

        // signUp 실패 시나리오 테스트
        val result = profileRepositoryImpl.signUp("test@example.com", "password", "password").first()
        assertTrue(result.isFailure)
        assertEquals("사용자 ID를 얻을 수 없습니다.", result.exceptionOrNull()?.message)
    }
}