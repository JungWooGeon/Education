package com.pass.presentation.intent

sealed class LiveListIntent {
    data object GetLiveList : LiveListIntent()
    data object StartLiveStreamingActivity : LiveListIntent()
    data class OnClickLiveStreamingItem(val broadcastId: String) : LiveListIntent()
}