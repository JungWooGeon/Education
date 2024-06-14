package com.pass.presentation.state.screen

import javax.annotation.concurrent.Immutable

@Immutable
data class VideoStreamingState(
    val isMinimized: Boolean = false,

    val videoTitle: String = "",
    val videoUrl: String = "",
    val userProfileURL: String = "",
    val userName: String = ""
)