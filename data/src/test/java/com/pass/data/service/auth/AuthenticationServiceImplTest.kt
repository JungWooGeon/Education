package com.pass.data.service.auth

import com.google.firebase.auth.FirebaseAuth
import com.pass.data.service.auth.AuthenticationServiceImpl
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import org.junit.Test

class AuthenticationServiceImplTest {

    private val mockAuth = mockk<FirebaseAuth>()
    private val authenticationServiceImpl = AuthenticationServiceImpl(mockAuth)

    @Test
    fun testSuccessGetCurrentUserId() {
        every { mockAuth.currentUser?.uid } returns "testId"

        val uid = authenticationServiceImpl.getCurrentUserId()
        assertEquals(uid, "testId")
    }
}