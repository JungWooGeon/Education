package com.pass.presentation.intent

sealed class MyIntent {
    data class NavigateScreenRoute(val screenRoute: String) : MyIntent()
}