package com.pass.presentation.sideeffect

sealed interface SignUpSideEffect {
    data object NavigateToProfileScreen : SignUpSideEffect
    data class Toast(val message: String) : SignUpSideEffect
}