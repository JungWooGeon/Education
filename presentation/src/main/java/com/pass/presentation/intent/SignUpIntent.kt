package com.pass.presentation.intent

sealed class SignUpIntent {
    data class OnClickSignUp(val id: String, val password: String, val verifyPassword: String) : SignUpIntent()
}