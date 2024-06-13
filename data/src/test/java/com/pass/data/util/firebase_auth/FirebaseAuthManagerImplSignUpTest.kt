package com.pass.data.util.firebase_auth

import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.pass.data.manager.database.FirebaseAuthManagerImpl
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FirebaseAuthManagerImplSignUpTest {

    // firebase 모킹
    private val mockFirebaseAuth = mockk<FirebaseAuth>()
    private val mockFirebaseUser = mockk<FirebaseUser>()

    // Task 객체 모킹
    private val mockTask = mockk<Task<AuthResult>>()
    private val mockTaskVoid = mockk<Task<Void>>()
    private val mockAuthResult = mockk<AuthResult>()

    private val firebaseAuthService = FirebaseAuthManagerImpl(mockFirebaseAuth)

    @Test
    fun testSuccessSignUp() = runBlocking {
        val testId = "test1234"
        val testPassword = "test1234"

        every { mockFirebaseAuth.currentUser } returns mockFirebaseUser
        every { mockFirebaseUser.uid } returns ""
        every { mockTask.result } returns mockAuthResult
        every { mockTaskVoid.isSuccessful } returns true

        every { mockTask.addOnCompleteListener(any()) } answers {
            firstArg<OnCompleteListener<Void>>().onComplete(mockTaskVoid)
            mockTask
        }

        every { mockFirebaseAuth.createUserWithEmailAndPassword(any(), any()) } answers { mockTask }

        val result = firebaseAuthService.signUp(testId, testPassword, testPassword).first()
        assertTrue(result.isSuccess)
    }

    @Test
    fun testFailSignUp() = runBlocking {
        val testId = "test1234"
        val testPassword = "test1234"
        val testError = "test error"

        every { mockTaskVoid.exception } returns Exception(testError)
        every { mockTaskVoid.isSuccessful } returns false

        every { mockTask.addOnCompleteListener(any()) } answers {
            firstArg<OnCompleteListener<Void>>().onComplete(mockTaskVoid)
            mockTask
        }

        every { mockFirebaseAuth.createUserWithEmailAndPassword(any(), any()) } answers { mockTask }

        val result = firebaseAuthService.signUp(testId, testPassword, testPassword).first()
        assertTrue(result.isFailure)
        assertEquals(result.exceptionOrNull()?.message, testError)
    }

    @Test
    fun testFailSignUpWithEmptyIdOrPasswordOrVerifyPassword() = runBlocking {
        val result = firebaseAuthService.signUp("", "", "").first()
        assertTrue(result.isFailure)
        assertEquals(result.exceptionOrNull()?.message, "아이디와 비밀번호를 입력해주세요.")

        val result2 = firebaseAuthService.signUp("test1234", "", "").first()
        assertTrue(result2.isFailure)
        assertEquals(result2.exceptionOrNull()?.message, "아이디와 비밀번호를 입력해주세요.")

        val result3 = firebaseAuthService.signUp("", "test1234", "test1234").first()
        assertTrue(result3.isFailure)
        assertEquals(result3.exceptionOrNull()?.message, "아이디와 비밀번호를 입력해주세요.")
    }

    @Test
    fun testFailSignUpWithNotEqualPassword() = runBlocking {
        val result = firebaseAuthService.signUp("test1234", "test1234", "t").first()
        assertTrue(result.isFailure)
        assertEquals(result.exceptionOrNull()?.message, "비밀번호가 맞지 않습니다.")
    }
}