package com.pass.presentation.sideeffect

sealed interface ProfileSideEffect {
    data class Toast(val message: String) : ProfileSideEffect
    data object NavigateSignInScreen : ProfileSideEffect
    data class NavigateToAddVideoScreen(val uri: String) : ProfileSideEffect
}