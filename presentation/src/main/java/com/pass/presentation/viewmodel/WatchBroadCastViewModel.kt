package com.pass.presentation.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import com.pass.domain.usecase.StopViewingUseCase
import com.pass.domain.usecase.WatchLiveStreamingUseCase
import com.pass.presentation.intent.WatchBroadCastIntent
import com.pass.presentation.sideeffect.WatchBroadCastSideEffect
import com.pass.presentation.state.loading.VideoTrackState
import com.pass.presentation.state.screen.WatchBroadCastState
import dagger.hilt.android.lifecycle.HiltViewModel
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import org.webrtc.VideoTrack
import javax.inject.Inject

@HiltViewModel
class WatchBroadCastViewModel @Inject constructor(
    private val watchLiveStreamingUseCase: WatchLiveStreamingUseCase<VideoTrack, Bitmap>,
    private val stopViewingUseCase: StopViewingUseCase<VideoTrack, Bitmap>
) : ViewModel(), ContainerHost<WatchBroadCastState, WatchBroadCastSideEffect> {

    override val container: Container<WatchBroadCastState, WatchBroadCastSideEffect> = container(
        initialState = WatchBroadCastState()
    )

    fun processIntent(intent: WatchBroadCastIntent) {
        when(intent) {
            is WatchBroadCastIntent.StartViewing -> startViewing(intent.broadcastId)
            is WatchBroadCastIntent.OnDismissRequest -> onDismissRequest()
            is WatchBroadCastIntent.OnExitRequest -> onExitRequest()
            is WatchBroadCastIntent.OnClickBackButton -> onClickBackButton()
        }
    }

    private fun startViewing(broadcastId: String) = intent {
        watchLiveStreamingUseCase(broadcastId).collect { result ->
            result.onSuccess { videoTrack ->
                reduce {
                    state.copy(videoTrackState = VideoTrackState.OnSuccess(videoTrack))
                }
            }.onFailure {
                reduce {
                    state.copy(videoTrackState = VideoTrackState.OnFailure)
                }
            }
        }
    }

    private fun onDismissRequest() = intent {
        reduce {
            state.copy(isExitDialog = false)
        }
    }

    private fun onExitRequest() = intent {
        postSideEffect(WatchBroadCastSideEffect.SuccessStopLiveStreaming)
    }

    private fun onClickBackButton() = intent {
        reduce {
            state.copy(isExitDialog = true)
        }

        // webrtc release
        stopViewingUseCase()
    }
}