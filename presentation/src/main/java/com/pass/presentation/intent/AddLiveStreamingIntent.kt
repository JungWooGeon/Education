package com.pass.presentation.intent

sealed class AddLiveStreamingIntent {
    data class OnChangeLiveStreamingTitle(val title: String) : AddLiveStreamingIntent()
    data object OnClickStartLiveStreamingButton : AddLiveStreamingIntent()
    data object OnClickBackButtonDuringLiveStreaming : AddLiveStreamingIntent()
    data object OnDismissRequest : AddLiveStreamingIntent()
    data object OnExitRequest : AddLiveStreamingIntent()
}