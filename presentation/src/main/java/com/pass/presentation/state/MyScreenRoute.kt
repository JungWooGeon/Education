package com.pass.presentation.state

sealed class MyScreenRoute(
    val screenRoute: String
) {
    companion object {
        const val SIGN_IN_SCREEN_ROUTE = "SignInScreenRoute"
        const val SIGN_UP_SCREEN_ROUTE = "SignUpScreenRoute"
        const val PROFILE_SCREEN_ROUTE = "ProfileScreenRoute"
    }

    data object SignInScreen : MyScreenRoute(screenRoute = SIGN_IN_SCREEN_ROUTE)

    data object SignUpScreen : MyScreenRoute(screenRoute = SIGN_UP_SCREEN_ROUTE)

    data object ProfileScreen : MyScreenRoute(screenRoute = PROFILE_SCREEN_ROUTE)
}