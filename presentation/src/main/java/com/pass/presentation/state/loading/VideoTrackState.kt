package com.pass.presentation.state.loading

import org.webrtc.VideoTrack

sealed interface VideoTrackState {
    data class OnSuccess(val videoTrack: VideoTrack) : VideoTrackState
    data object OnFailure : VideoTrackState
    data object OnLoading : VideoTrackState
}