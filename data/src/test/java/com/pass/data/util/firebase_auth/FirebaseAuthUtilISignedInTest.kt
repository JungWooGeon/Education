package com.pass.data.util.firebase_auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.pass.data.util.FirebaseAuthUtil
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test

class FirebaseAuthUtilISignedInTest {

    // firebase 모킹
    private val mockFirebaseAuth = mockk<FirebaseAuth>()
    private val mockFirebaseUser = mockk<FirebaseUser>()

    private val firebaseAuthUtil = FirebaseAuthUtil(mockFirebaseAuth)

    @Test
    fun testSuccessIsSignedIn() = runBlocking {
        every { mockFirebaseAuth.currentUser } returns mockFirebaseUser
        val result = firebaseAuthUtil.isSignedIn().first()
        Assert.assertTrue(result)
    }

    @Test
    fun testFailIsSignedIn() = runBlocking {
        every { mockFirebaseAuth.currentUser } returns null
        val result = firebaseAuthUtil.isSignedIn().first()
        Assert.assertFalse(result)
    }
}