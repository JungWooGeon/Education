package com.pass.presentation.sideeffect

sealed interface MyScreenSideEffect {
    data class NavigateScreenRoute(val screenRoute: String) : MyScreenSideEffect
}