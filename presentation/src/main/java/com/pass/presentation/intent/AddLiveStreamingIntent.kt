package com.pass.presentation.intent

import android.graphics.Bitmap

sealed class AddLiveStreamingIntent {
    data class OnChangeLiveStreamingTitle(val title: String) : AddLiveStreamingIntent()
    data class OnClickStartLiveStreamingButton(val thumbnailImage: Bitmap?) : AddLiveStreamingIntent()
    data object OnClickBackButtonDuringLiveStreaming : AddLiveStreamingIntent()
    data object OnDismissRequest : AddLiveStreamingIntent()
    data object OnExitRequest : AddLiveStreamingIntent()
}