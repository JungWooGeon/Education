package com.pass.presentation.intent

import com.pass.domain.model.Video

sealed class VideoListIntent {
    data object ReadVideoList : VideoListIntent()
    data class OnClickVideoItem(val video: Video) : VideoListIntent()
}