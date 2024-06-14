package com.pass.presentation.sideeffect

sealed interface MainScreenSideEffect {
    data class Toast(val message: String) : MainScreenSideEffect
    data object FinishActivity : MainScreenSideEffect
    data class NavigateScreenRoute(val screenRoute: String) : MainScreenSideEffect
}