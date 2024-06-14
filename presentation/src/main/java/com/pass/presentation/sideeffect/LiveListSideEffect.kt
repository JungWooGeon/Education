package com.pass.presentation.sideeffect

sealed interface LiveListSideEffect {
    data object StartLiveStreamingActivity : LiveListSideEffect
    data object NavigateLogInScreen : LiveListSideEffect
    data class Toast(val message: String) : LiveListSideEffect
    data class StartWatchBroadCastActivity(val broadcastId: String) : LiveListSideEffect
}