package com.pass.presentation.sideeffect

import com.pass.domain.model.Video

sealed interface VideoListSideEffect {
    data class Toast(val message: String) : VideoListSideEffect
    data class ShowVideoStreamingPlayer(val video: Video) : VideoListSideEffect
    data object NavigateLogInScreen : VideoListSideEffect
}