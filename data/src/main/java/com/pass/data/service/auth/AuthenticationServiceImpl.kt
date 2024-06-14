package com.pass.data.service.auth

import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject

class AuthenticationServiceImpl @Inject constructor(
    private val auth: FirebaseAuth
) : AuthenticationService {
    override fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }
}