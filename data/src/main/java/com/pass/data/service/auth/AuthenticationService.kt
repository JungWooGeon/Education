package com.pass.data.service.auth

interface AuthenticationService {
    fun getCurrentUserId(): String?
}