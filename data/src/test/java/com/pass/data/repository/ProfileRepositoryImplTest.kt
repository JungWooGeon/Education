package com.pass.data.repository

import com.pass.data.service.auth.AuthenticationServiceImpl
import com.pass.data.service.auth.SignServiceImpl
import com.pass.data.service.database.UserServiceImpl
import com.pass.domain.model.Profile
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Test

class ProfileRepositoryImplTest {

    private val mockSignService = mockk<SignServiceImpl>()
    private val mockUserService = mockk<UserServiceImpl>()
    private val mockAuthenticationService = mockk<AuthenticationServiceImpl>()
    private val mockProfile = mockk<Profile>()

    private val profileRepositoryImpl = ProfileRepositoryImpl(mockSignService, mockUserService, mockAuthenticationService)

    @Test
    fun testSuccessIsSignedIn() = runBlocking {
        coEvery { mockSignService.isSignedIn() } returns flowOf(true)

        val result = profileRepositoryImpl.isSignedIn().first()
        assertTrue(result)
    }

    @Test
    fun testSuccessSignIn() = runBlocking {
        coEvery { mockSignService.signIn(any(), any()) } returns flowOf(Result.success(Unit))

        val result = profileRepositoryImpl.signIn("testId", "testPassword").first()
        assertTrue(result.isSuccess)
    }

    @Test
    fun testSuccessSignUp() = runBlocking {
        coEvery { mockSignService.signUp(any(), any(), any()) } returns flowOf(Result.success(Unit))

        val result = profileRepositoryImpl.signUp("testId", "testPassword", "testPassword").first()
        assertTrue(result.isSuccess)
    }

    @Test
    fun testSuccessSignOut() = runBlocking {
        coEvery { mockSignService.signOut() } just runs

        profileRepositoryImpl.signOut()
        coVerify { mockSignService.signOut() }
    }

    @Test
    fun testSuccessGetUserProfile() = runBlocking {
        coEvery { mockAuthenticationService.getCurrentUserId() } returns "testUserId"
        coEvery { mockUserService.getUserProfile(any()) } returns flowOf(Result.success(mockProfile))

        val result = profileRepositoryImpl.getUserProfile().first()
        assertTrue(result.isSuccess)
        assertEquals(result.getOrNull(), mockProfile)
    }

    @Test
    fun testFailGetUserProfileWithNullUid() = runBlocking {
        coEvery { mockAuthenticationService.getCurrentUserId() } returns null

        val result = profileRepositoryImpl.getUserProfile().first()
        assertTrue(result.isFailure)
        assertEquals(result.exceptionOrNull()?.message, "오류가 발생하였습니다. 다시 로그인을 진행해주세요.")
    }

    @Test
    fun testSuccessUpdateUserProfile() = runBlocking {
        coEvery { mockUserService.updateUserProfile(any(), any()) } returns flowOf(Result.success(Unit))

        val result = profileRepositoryImpl.updateUserProfile("testName", "testField").first()
        assertTrue(result.isSuccess)
    }

    @Test
    fun testSuccessUpdateUserProfilePicture() = runBlocking {
        coEvery { mockAuthenticationService.getCurrentUserId() } returns "testUserId"
        coEvery { mockUserService.updateUserProfilePicture(any(), any()) } returns flowOf(Result.success(""))

        val result = profileRepositoryImpl.updateUserProfilePicture("testPictureUri").first()
        assertTrue(result.isSuccess)
    }

    @Test
    fun testFailUpdateUserProfilePictureWithNullUid() = runBlocking {
        coEvery { mockAuthenticationService.getCurrentUserId() } returns null

        val result = profileRepositoryImpl.updateUserProfilePicture("testPictureUri").first()
        assertTrue(result.isFailure)
        assertEquals(result.exceptionOrNull()?.message, "오류가 발생하였습니다. 다시 로그인을 진행해주세요.")
    }

    @Test
    fun testSuccessGetOtherUserProfile() = runBlocking {
        coEvery { mockUserService.getOtherUserProfile(any()) } returns flowOf(Result.success(mockProfile))

        val result = profileRepositoryImpl.getOtherUserProfile("testUserId").first()
        assertTrue(result.isSuccess)
    }
}