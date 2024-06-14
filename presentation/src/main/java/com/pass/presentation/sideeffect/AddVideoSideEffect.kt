package com.pass.presentation.sideeffect

sealed interface AddVideoSideEffect {
    data class Toast(val message: String) : AddVideoSideEffect
    data object NavigateProfileScreen : AddVideoSideEffect
}