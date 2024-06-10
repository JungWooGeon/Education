package com.pass.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pass.domain.usecase.StopViewingUseCase
import com.pass.domain.usecase.WatchLiveStreamingUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import org.webrtc.VideoTrack
import javax.annotation.concurrent.Immutable
import javax.inject.Inject

@HiltViewModel
class WatchBroadCastViewModel @Inject constructor(
    private val watchLiveStreamingUseCase: WatchLiveStreamingUseCase<VideoTrack>,
    private val stopViewingUseCase: StopViewingUseCase<VideoTrack>
) : ViewModel(), ContainerHost<WatchBroadCastState, WatchBroadCastSideEffect> {

    override val container: Container<WatchBroadCastState, WatchBroadCastSideEffect> = container(
        initialState = WatchBroadCastState()
    )

    private val _videoTrackState = MutableStateFlow<VideoTrackState>(VideoTrackState.OnLoading)
    val videoTrackState: StateFlow<VideoTrackState> = _videoTrackState

    fun startViewing(broadcastId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            watchLiveStreamingUseCase(broadcastId).collect { result ->
                result.onSuccess { videoTrack ->
                    println("성공")
                    _videoTrackState.value = VideoTrackState.OnSuccess(videoTrack)
                }.onFailure {
                    println("실패")
                    _videoTrackState.value = VideoTrackState.OnFailure
                }
            }
        }
    }

    fun onDismissRequest() = intent {
        reduce {
            state.copy(isExitDialog = false)
        }
    }

    fun onExitRequest() = intent {
        postSideEffect(WatchBroadCastSideEffect.SuccessStopLiveStreaming)
    }

    fun onClickBackButton() = intent {
        reduce {
            state.copy(isExitDialog = true)
        }

        // webrtc release
        stopViewingUseCase()
    }
}

@Immutable
data class WatchBroadCastState(
    val isExitDialog: Boolean = false
)

sealed interface WatchBroadCastSideEffect {
    data object SuccessStopLiveStreaming : WatchBroadCastSideEffect
}

sealed interface VideoTrackState {
    data class OnSuccess(val videoTrack: VideoTrack) : VideoTrackState
    data object OnFailure : VideoTrackState
    data object OnLoading : VideoTrackState
}