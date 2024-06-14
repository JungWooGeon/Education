package com.pass.presentation.state.screen

import javax.annotation.concurrent.Immutable

@Immutable
data class SignUpState(
    val id: String = "",
    val password: String = "",
    val verifyPassword: String = ""
)