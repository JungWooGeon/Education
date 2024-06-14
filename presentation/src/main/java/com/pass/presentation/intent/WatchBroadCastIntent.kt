package com.pass.presentation.intent

sealed class WatchBroadCastIntent {
    data class StartViewing(val broadcastId: String) : WatchBroadCastIntent()
    data object OnDismissRequest : WatchBroadCastIntent()
    data object OnExitRequest : WatchBroadCastIntent()
    data object OnClickBackButton : WatchBroadCastIntent()
}