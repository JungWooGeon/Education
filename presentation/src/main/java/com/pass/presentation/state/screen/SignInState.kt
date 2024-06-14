package com.pass.presentation.state.screen

import javax.annotation.concurrent.Immutable

@Immutable
data class SignInState(
    val id: String = "",
    val password: String = ""
)