package com.pass.presentation.state.screen

import org.webrtc.VideoTrack
import javax.annotation.concurrent.Immutable

@Immutable
data class AddLiveStreamingState(
    val userProfileUrl: String = "",
    val profileName: String = "",
    val isLiveStreaming: Boolean = false,
    val isExitDialog: Boolean = false,
    val videoTrack: VideoTrack? = null
)