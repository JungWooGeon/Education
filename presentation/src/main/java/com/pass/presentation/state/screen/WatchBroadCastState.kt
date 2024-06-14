package com.pass.presentation.state.screen

import com.pass.presentation.state.loading.VideoTrackState
import javax.annotation.concurrent.Immutable

@Immutable
data class WatchBroadCastState(
    val isExitDialog: Boolean = false,
    val videoTrackState: VideoTrackState = VideoTrackState.OnLoading
)