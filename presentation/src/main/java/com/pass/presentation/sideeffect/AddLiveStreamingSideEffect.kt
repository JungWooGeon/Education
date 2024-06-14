package com.pass.presentation.sideeffect

sealed interface AddLiveStreamingSideEffect {
    data object FailGetUserProfile : AddLiveStreamingSideEffect
    data class FailCamera(val errorMessage: String) : AddLiveStreamingSideEffect
    data object SuccessStartLiveStreaming : AddLiveStreamingSideEffect
    data object SuccessStopLiveStreaming : AddLiveStreamingSideEffect
}