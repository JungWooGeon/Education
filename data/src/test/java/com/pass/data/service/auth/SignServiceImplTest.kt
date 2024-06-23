package com.pass.data.service.auth

import com.pass.data.manager.database.FirebaseAuthManagerImpl
import com.pass.data.manager.database.FirebaseDatabaseManagerImpl
import com.pass.data.util.FireStoreUtil
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Test

class SignServiceImplTest {

    private val mockFirebaseAuthManager = mockk<FirebaseAuthManagerImpl>()
    private val mockFirebaseDatabaseManager = mockk<FirebaseDatabaseManagerImpl>()
    private val mockFireStoreUtil = mockk<FireStoreUtil>()

    private val signServiceImpl = SignServiceImpl(mockFirebaseAuthManager, mockFirebaseDatabaseManager, mockFireStoreUtil)

    @Test
    fun testSuccessIsSingedIn() = runBlocking {
        coEvery { mockFirebaseAuthManager.isSignedIn() } returns flowOf(true)
        val result = signServiceImpl.isSignedIn().first()
        assertTrue(result)
    }

    @Test
    fun testSuccessSignIn() = runBlocking {
        coEvery { mockFirebaseAuthManager.signIn(any(), any()) } returns flowOf(Result.success(Unit))
        val result = signServiceImpl.signIn("testId", "testPassword").first()
        assertTrue(result.isSuccess)
    }

    @Test
    fun testSuccessSignUp() = runBlocking {
        val testHashMap = hashMapOf("name" to "testName", "pictureUrl" to "testPictureUrl")

        coEvery { mockFirebaseAuthManager.signUp(any(), any(), any()) } returns flowOf(Result.success("testUid"))
        every { mockFireStoreUtil.createUserProfileData(any(), any()) } returns testHashMap
        coEvery { mockFirebaseDatabaseManager.createData(testHashMap, "profiles", "testUid") } returns flowOf(Result.success(Unit))
        val result = signServiceImpl.signUp("testId", "testPassword", "testPassword").first()
        assertTrue(result.isSuccess)
    }

    @Test
    fun testFailSignUp() = runBlocking {
        coEvery { mockFirebaseAuthManager.signUp(any(), any(), any()) } returns flowOf(Result.failure(Exception("test failed")))
        val result = signServiceImpl.signUp("testId", "testPassword", "testPassword").first()

        assertTrue(result.isFailure)
        assertEquals(result.exceptionOrNull()?.message, "test failed")
    }

    @Test
    fun testSuccessSignOut() = runBlocking {
        coEvery { mockFirebaseAuthManager.signOut() } just runs
        signServiceImpl.signOut()
        coVerify { mockFirebaseAuthManager.signOut() }
    }
}