package com.pass.data.util.firebase_auth

import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.pass.data.manager.database.FirebaseAuthManagerImpl
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test

class FirebaseAuthManagerImplSignInTest {

    // firebase 모킹
    private val mockFirebaseAuth = mockk<FirebaseAuth>()

    // Task 객체 모킹
    private val mockTask = mockk<Task<AuthResult>>()
    private val mockTaskVoid = mockk<Task<Void>>()
    private val mockAuthResult = mockk<AuthResult>()

    private val firebaseAuthService = FirebaseAuthManagerImpl(mockFirebaseAuth)

    @Test
    fun testSuccessSignIn() = runBlocking {
        val testId = "test1234"
        val testPassword = "test1234"

        every { mockTask.result } returns mockAuthResult
        every { mockTaskVoid.isSuccessful } returns true

        every { mockTask.addOnCompleteListener(any()) } answers {
            firstArg<OnCompleteListener<Void>>().onComplete(mockTaskVoid)
            mockTask
        }

        every { mockFirebaseAuth.signInWithEmailAndPassword(any(), any()) } answers { mockTask }

        val result = firebaseAuthService.signIn(testId, testPassword).first()
        Assert.assertTrue(result.isSuccess)
    }

    @Test
    fun testFailSignIn() = runBlocking {
        val testId = "test1234"
        val testPassword = "test1234"
        val testError = "test error"

        every { mockTask.result } returns mockAuthResult
        every { mockTaskVoid.isSuccessful } returns false
        every { mockTaskVoid.exception } returns Exception(testError)

        every { mockTask.addOnCompleteListener(any()) } answers {
            firstArg<OnCompleteListener<Void>>().onComplete(mockTaskVoid)
            mockTask
        }

        every { mockFirebaseAuth.signInWithEmailAndPassword(any(), any()) } answers { mockTask }

        val result = firebaseAuthService.signIn(testId, testPassword).first()
        Assert.assertTrue(result.isFailure)
        Assert.assertEquals(result.exceptionOrNull()?.message, testError)
    }

    @Test
    fun testFailSignInWithEmptyIdOrPassword() = runBlocking {
        val result = firebaseAuthService.signIn("", "").first()
        Assert.assertTrue(result.isFailure)
        Assert.assertEquals(result.exceptionOrNull()?.message, "아이디와 비밀번호를 입력해주세요.")

        val result2 = firebaseAuthService.signIn("test1234", "").first()
        Assert.assertTrue(result2.isFailure)
        Assert.assertEquals(result2.exceptionOrNull()?.message, "아이디와 비밀번호를 입력해주세요.")

        val result3 = firebaseAuthService.signIn("", "test1234").first()
        Assert.assertTrue(result3.isFailure)
        Assert.assertEquals(result3.exceptionOrNull()?.message, "아이디와 비밀번호를 입력해주세요.")
    }
}