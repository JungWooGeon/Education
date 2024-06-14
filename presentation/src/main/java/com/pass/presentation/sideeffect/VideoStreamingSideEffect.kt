package com.pass.presentation.sideeffect

sealed interface VideoStreamingSideEffect {
    data class FailVideoStreaming(val errorMessage: String) : VideoStreamingSideEffect
}