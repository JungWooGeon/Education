package com.pass.presentation.state

sealed class MyScreenRoute(
    val screenRoute: String
) {
    companion object {
        const val LOGIN_SCREEN_ROUTE = "LoginScreenRoute"
        const val PROFILE_SCREEN_ROUTE = "ProfileScreenRoute"
    }

    data object LoginScreen : MyScreenRoute(screenRoute = LOGIN_SCREEN_ROUTE)

    data object ProfileScreen : MyScreenRoute(screenRoute = PROFILE_SCREEN_ROUTE)
}