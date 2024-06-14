package com.pass.presentation.state.screen

import javax.annotation.concurrent.Immutable

@Immutable
data class MyScreenState(
    val isSignedInState: Boolean? = null
)