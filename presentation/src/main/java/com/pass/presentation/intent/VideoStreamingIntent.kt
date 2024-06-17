package com.pass.presentation.intent

import com.pass.domain.model.Video

sealed class VideoStreamingIntent {
    data class InitContent(val video: Video) : VideoStreamingIntent()
    data object OnChangeMiniPlayer : VideoStreamingIntent()
    data object OnChangeDefaultPlayer : VideoStreamingIntent()
    data class OnChangeIsFullScreen(val currentPosition: Long?) : VideoStreamingIntent()
    data class UpdatePlaybackPosition(val currentPosition: Long) : VideoStreamingIntent()
}