package com.pass.data.repository.profile

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.storage.FirebaseStorage
import com.pass.data.di.DateTimeProvider
import com.pass.data.repository.ProfileRepositoryImpl
import com.pass.data.util.CalculateUtil
import com.pass.data.util.FirebaseAuthUtil
import com.pass.data.util.FirebaseDatabaseUtil
import com.pass.data.util.FirebaseStorageUtil
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProfileRepositoryGetOtherUserProfileTest {

    // firebase 모킹
    private val auth = mockk<FirebaseAuth>()
    private val storage = mockk<FirebaseStorage>()
    private val firebaseDatabaseUtil = mockk<FirebaseDatabaseUtil>()

    // util 선언
    private val firebaseAuthUtil = FirebaseAuthUtil(auth)

    private val firebaseStorageUtil = FirebaseStorageUtil(storage)
    private val calculationUtil = CalculateUtil(dateTimeProvider = DateTimeProvider())

    private val mockDocumentSnapshot = mockk<DocumentSnapshot>()

    private val testString = "testString"
    private val testUserId = "testUserId"

    // repository 초기화
    private val profileRepositoryImpl = ProfileRepositoryImpl(auth, firebaseAuthUtil, firebaseDatabaseUtil, firebaseStorageUtil, calculationUtil)

    @Test
    fun testSuccessGetOtherUserProfile() = runBlocking {
        every { mockDocumentSnapshot.getString(any()) } returns testString

        val successFlow: Flow<Result<DocumentSnapshot>> = flow {
            emit(Result.success(mockDocumentSnapshot))
        }

        coEvery { firebaseDatabaseUtil.readData(any(), any()) } returns successFlow

        val result = profileRepositoryImpl.getOtherUserProfile(testUserId).first()

        assertTrue(result.isSuccess)
        assertEquals(result.getOrNull()?.name, testString)
        assertEquals(result.getOrNull()?.pictureUrl, testString)
    }

    @Test
    fun testFailGetOtherUserProfile() = runBlocking {
        val testException = Exception("Test Error")

        every { mockDocumentSnapshot.getString(any()) } returns testString

        val failureFlow: Flow<Result<DocumentSnapshot>> = flow {
            emit(Result.failure(testException))
        }

        coEvery { firebaseDatabaseUtil.readData(any(), any()) } returns failureFlow

        val result = profileRepositoryImpl.getOtherUserProfile(testUserId).first()

        assertTrue(result.isFailure)
        assertEquals(result.exceptionOrNull()?.message, testException.message)
    }

    @Test
    fun testFailGetOtherUserProfileWithNameOrPictureNull() = runBlocking {
        every { mockDocumentSnapshot.getString(any()) } returns null

        val successFlow: Flow<Result<DocumentSnapshot>> = flow {
            emit(Result.success(mockDocumentSnapshot))
        }

        coEvery { firebaseDatabaseUtil.readData(any(), any()) } returns successFlow

        val result = profileRepositoryImpl.getOtherUserProfile(testUserId).first()

        assertTrue(result.isFailure)
        assertEquals(result.exceptionOrNull()?.message, "프로필을 조회할 수 없습니다.")
    }
}