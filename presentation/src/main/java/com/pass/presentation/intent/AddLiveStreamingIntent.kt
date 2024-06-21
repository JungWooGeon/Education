package com.pass.presentation.intent

import android.graphics.Bitmap

sealed class AddLiveStreamingIntent {
    data class OnClickStartLiveStreamingButton(val thumbnailImage: Bitmap?, val title: String) : AddLiveStreamingIntent()
    data object OnClickBackButtonDuringLiveStreaming : AddLiveStreamingIntent()
    data object OnDismissRequest : AddLiveStreamingIntent()
    data object OnExitRequest : AddLiveStreamingIntent()
}