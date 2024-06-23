package com.pass.data.service.database

import com.google.firebase.firestore.DocumentSnapshot
import com.pass.data.manager.database.FirebaseDatabaseManagerImpl
import com.pass.data.manager.database.FirebaseStorageManagerImpl
import com.pass.data.util.CalculateUtil
import com.pass.data.util.FireStoreUtil
import com.pass.domain.model.Profile
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Test

class UserServiceImplTest {

    private val mockFirebaseDatabaseManager = mockk<FirebaseDatabaseManagerImpl>()
    private val mockFirebaseStorageManager = mockk<FirebaseStorageManagerImpl>()
    private val mockCalculateUtil = mockk<CalculateUtil>()
    private val mockFireStoreUtil = mockk<FireStoreUtil>()
    private val mockDocumentSnapshot = mockk<DocumentSnapshot>()
    private val mockProfile = mockk<Profile>()

    private val userServiceImpl = UserServiceImpl(mockFirebaseDatabaseManager, mockFirebaseStorageManager, mockCalculateUtil, mockFireStoreUtil)

    @Test
    fun testSuccessGetUserProfile() = runBlocking {
        every { mockCalculateUtil.calculateAgoTime(any()) } returns ""
        coEvery { mockFirebaseDatabaseManager.readData("profiles", "testUserId") } returns flowOf(Result.success(mockDocumentSnapshot))
        coEvery { mockFirebaseDatabaseManager.readDataList("profiles", "testUserId", "videos") } returns flowOf(Result.success(
            listOf(mockDocumentSnapshot, mockDocumentSnapshot, mockDocumentSnapshot)
        ))

        val calculateAgoTimeSlot = slot<(String?) -> String>()
        every { mockFireStoreUtil.extractProfileFromProfileAndVideoDocuments(
            userId = "testUserId",
            calculateAgoTime = capture(calculateAgoTimeSlot),
            any(), any()
        ) } answers {
            calculateAgoTimeSlot.captured.invoke("testString")
            Result.success(mockProfile)
        }

        val result = userServiceImpl.getUserProfile("testUserId").first()

        assertTrue(result.isSuccess)
    }

    @Test
    fun testFailGetUserProfileWithFailReadProfileInfoFlow() = runBlocking {
        coEvery { mockFirebaseDatabaseManager.readData("profiles", "testUserId") } returns flowOf(Result.failure(Exception("test failed")))
        coEvery { mockFirebaseDatabaseManager.readDataList("profiles", "testUserId", "videos") } returns flowOf(Result.success(
            listOf(mockDocumentSnapshot, mockDocumentSnapshot, mockDocumentSnapshot)
        ))

        val result = userServiceImpl.getUserProfile("testUserId").first()

        assertTrue(result.isFailure)
        assertEquals(result.exceptionOrNull()?.message, "test failed")
    }

    @Test
    fun testFailGetUserProfileWithFailReadProfileVideoListFlow() = runBlocking {
        coEvery { mockFirebaseDatabaseManager.readData("profiles", "testUserId") } returns flowOf(Result.success(mockDocumentSnapshot))
        coEvery { mockFirebaseDatabaseManager.readDataList("profiles", "testUserId", "videos") } returns flowOf(Result.failure(Exception("test failed")))

        val result = userServiceImpl.getUserProfile("testUserId").first()

        assertTrue(result.isFailure)
        assertEquals(result.exceptionOrNull()?.message, "test failed")
    }

    @Test
    fun testSuccessUpdateUserProfile() = runBlocking {
        coEvery { mockFirebaseDatabaseManager.updateData(any(), any(), any()) } returns flowOf(Result.success(Unit))
        val result = userServiceImpl.updateUserProfile("", "").first()

        assertTrue(result.isSuccess)
    }

    @Test
    fun testSuccessUpdateUserProfilePicture() = runBlocking {
        coEvery { mockFirebaseStorageManager.updateFile(any(), any()) } returns flowOf(Result.success("testSuccess"))
        coEvery { mockFirebaseDatabaseManager.updateData(any(), any(), any()) } returns flowOf(Result.success(Unit))

        val result = userServiceImpl.updateUserProfilePicture("", "").first()

        assertTrue(result.isSuccess)
        assertEquals(result.getOrNull(), "testSuccess")
    }

    @Test
    fun testFailUpdateUserProfilePictureWithFailUpdateFile() = runBlocking {
        coEvery { mockFirebaseStorageManager.updateFile(any(), any()) } returns flowOf(Result.failure(Exception("test failed")))

        val result = userServiceImpl.updateUserProfilePicture("", "").first()

        assertTrue(result.isFailure)
        assertEquals(result.exceptionOrNull()?.message, "test failed")
    }

    @Test
    fun testFailUpdateUserProfilePictureWithFailUpdateData() = runBlocking {
        coEvery { mockFirebaseStorageManager.updateFile(any(), any()) } returns flowOf(Result.success("testSuccess"))
        coEvery { mockFirebaseDatabaseManager.updateData(any(), any(), any()) } returns flowOf(Result.failure(Exception("test failed")))

        val result = userServiceImpl.updateUserProfilePicture("", "").first()

        assertTrue(result.isFailure)
        assertEquals(result.exceptionOrNull()?.message, "프로필 사진 업로드를 시도하던 중 오류가 발생하였습니다. 잠시 후 시도해주세요.")
    }

    @Test
    fun testSuccessGetOtherUserProfile() = runBlocking {
        coEvery { mockFirebaseDatabaseManager.readData("profiles", any()) } returns flowOf(Result.success(mockDocumentSnapshot))
        every { mockFireStoreUtil.createProfileFromDocumentSnapShot(any()) } returns Result.success(mockProfile)

        val result = userServiceImpl.getOtherUserProfile("testUserId").first()

        assertTrue(result.isSuccess)
        assertEquals(result.getOrNull(), mockProfile)
    }

    @Test
    fun testFailGetOtherUserProfile() = runBlocking {
        coEvery { mockFirebaseDatabaseManager.readData("profiles", any()) } returns flowOf(Result.failure(Exception("test failed")))

        val result = userServiceImpl.getOtherUserProfile("testUserId").first()

        assertTrue(result.isFailure)
        assertEquals(result.exceptionOrNull()?.message, "test failed")
    }
}