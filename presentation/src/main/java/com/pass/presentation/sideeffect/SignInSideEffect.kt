package com.pass.presentation.sideeffect

sealed interface SignInSideEffect {
    data object NavigateToProfileScreen : SignInSideEffect
    data class Toast(val message: String) : SignInSideEffect
}