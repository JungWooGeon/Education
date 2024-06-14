package com.pass.presentation.intent

sealed class SignInIntent {
    data object OnClickSignIn : SignInIntent()
    data class OnChangeId(val id: String) : SignInIntent()
    data class OnChangePassword(val password: String) : SignInIntent()
}