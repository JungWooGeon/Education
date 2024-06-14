package com.pass.presentation.intent

import com.pass.domain.model.Video

sealed class MainIntent {
    data object OnClickBackButton : MainIntent()
    data object CloseVideoPlayer : MainIntent()
    data class ShowVideoPlayer(val video: Video) : MainIntent()
    data class NavigateScreenRoute(val screenRoute: String) : MainIntent()
}