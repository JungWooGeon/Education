package com.pass.data.repository.profile

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.pass.data.di.DateTimeProvider
import com.pass.data.repository.ProfileRepositoryImpl
import com.pass.data.util.CalculateUtil
import com.pass.data.util.FirebaseAuthUtil
import com.pass.data.util.FirebaseDatabaseUtil
import com.pass.data.util.FirebaseStorageUtil
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test

class ProfileRepositoryIsSignedInTest {

    // firebase 모킹
    private val auth = mockk<FirebaseAuth>()
    private val fireStore = mockk<FirebaseFirestore>()
    private val storage = mockk<FirebaseStorage>()

    // util 선언
    private val firebaseAuthUtil = FirebaseAuthUtil(auth)
    private val firebaseDatabaseUtil = FirebaseDatabaseUtil(auth, fireStore)
    private val firebaseStorageUtil = FirebaseStorageUtil(storage)
    private val calculationUtil = CalculateUtil(dateTimeProvider = DateTimeProvider())

    // repository 초기화
    private val profileRepositoryImpl = ProfileRepositoryImpl(auth, firebaseAuthUtil, firebaseDatabaseUtil, firebaseStorageUtil, calculationUtil)

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
}