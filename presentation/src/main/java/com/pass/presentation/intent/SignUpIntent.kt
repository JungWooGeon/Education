package com.pass.presentation.intent

sealed class SignUpIntent {
    data object OnClickSignUp : SignUpIntent()
    data class OnChangeId(val id: String) : SignUpIntent()
    data class OnChangePassword(val password: String) : SignUpIntent()
    data class OnChangeVerifyPassword(val verifyPassword: String) : SignUpIntent()
}