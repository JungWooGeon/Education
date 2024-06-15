package com.pass.data.manager.database.firebase_auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.pass.data.manager.database.FirebaseAuthManagerImpl
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test

class FirebaseAuthManagerImplISignedInTest {

    // firebase 모킹
    private val mockFirebaseAuth = mockk<FirebaseAuth>()
    private val mockFirebaseUser = mockk<FirebaseUser>()

    private val firebaseAuthService = FirebaseAuthManagerImpl(mockFirebaseAuth)

    @Test
    fun testSuccessIsSignedIn() = runBlocking {
        every { mockFirebaseAuth.currentUser } returns mockFirebaseUser
        val result = firebaseAuthService.isSignedIn().first()
        Assert.assertTrue(result)
    }

    @Test
    fun testFailIsSignedIn() = runBlocking {
        every { mockFirebaseAuth.currentUser } returns null
        val result = firebaseAuthService.isSignedIn().first()
        Assert.assertFalse(result)
    }
}