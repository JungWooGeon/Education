package com.pass.presentation.intent

sealed class SignInIntent {
    data class OnClickSignIn(val id: String, val password: String) : SignInIntent()
}