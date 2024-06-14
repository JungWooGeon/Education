package com.pass.presentation.sideeffect

sealed interface WatchBroadCastSideEffect {
    data object SuccessStopLiveStreaming : WatchBroadCastSideEffect
}