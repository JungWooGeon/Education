package com.pass.data

import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.pass.data.repository.ProfileRepositoryImpl
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test

class ProfileRepositorySignInTest {

    // firebase 모킹
    private val auth = mockk<FirebaseAuth>()
    private val fireStore = mockk<FirebaseFirestore>()

    // Task 객체 모킹
    private val mockException = Exception("signIn failed")
    private val mockTask = mockk<Task<AuthResult>>()
    private val mockAuthResult = mockk<AuthResult>()

    // repository 초기화
    private val profileRepositoryImpl = ProfileRepositoryImpl(auth, fireStore)

    @Test
    fun testSuccessIsSignedIn() = runBlocking {
        coEvery { auth.currentUser } returns mockk()

        // isSignedIn 성공 시나리오 테스트
        Assert.assertTrue(profileRepositoryImpl.isSignedIn().first())
    }

    @Test
    fun testFailIsSignedIn() = runBlocking {
        coEvery { auth.currentUser } returns null

        // isSignedIn 실패 시나리오 테스트
        Assert.assertFalse(profileRepositoryImpl.isSignedIn().first())
    }

    @Test
    fun testSuccessSignIn() = runBlocking {
        // 성공 시나리오 모킹
        every { mockTask.isSuccessful } returns true
        every { mockTask.result } returns mockAuthResult

        // addOnCompleteListener 메서드 호출에 대한 모킹
        every { mockTask.addOnCompleteListener(any()) } answers {
            firstArg<OnCompleteListener<AuthResult>>().onComplete(mockTask)
            mockTask
        }

        // firebase signInWithEmailAndPassword 메서드를 호출에 대한 모킹
        every { auth.signInWithEmailAndPassword(any(), any()) } returns mockTask

        // signIn 성공 시나리오 테스트
        val result = profileRepositoryImpl.signIn("test@example.com", "password").first()
        Assert.assertTrue(result.isSuccess)
    }

    @Test
    fun testFailSignIn() = runBlocking {
        // 실패 시나리오 모킹
        every { mockTask.isSuccessful } returns false
        every { mockTask.exception } returns mockException
        every { mockTask.result } returns mockAuthResult

        // addOnCompleteListener 메서드 호출에 대한 모킹
        every { mockTask.addOnCompleteListener(any()) } answers {
            firstArg<OnCompleteListener<AuthResult>>().onComplete(mockTask)
            mockTask
        }

        // firebase signInWithEmailAndPassword 메서드를 호출에 대한 모킹
        every { auth.signInWithEmailAndPassword(any(), any()) } returns mockTask

        // signIn 실패 시나리오 테스트
        val result = profileRepositoryImpl.signIn("test@example.com", "password").first()
        Assert.assertTrue(result.isFailure)
        Assert.assertEquals(mockException.message, result.exceptionOrNull()?.message)
    }

    @Test
    fun testFailSignInWithEmptyIdOrPassword() = runBlocking {
        // id 가 빈 값일 때 false 값과 예외 메시지를 반환하는지 테스트
        var result = profileRepositoryImpl.signIn("", "testPassword").first()
        Assert.assertTrue(result.isFailure)
        Assert.assertEquals("아이디와 비밀번호를 입력해주세요.", result.exceptionOrNull()?.message)

        // password 가 빈 값일 때 false 값과 예외 메시지를 반환하는지 테스트
        result = profileRepositoryImpl.signIn("testId@naver.com", "").first()
        Assert.assertTrue(result.isFailure)
        Assert.assertEquals("아이디와 비밀번호를 입력해주세요.", result.exceptionOrNull()?.message)
    }
}