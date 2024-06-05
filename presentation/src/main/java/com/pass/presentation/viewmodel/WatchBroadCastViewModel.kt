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
import org.webrtc.SurfaceViewRenderer
import org.webrtc.VideoTrack
import javax.inject.Inject

@HiltViewModel
class WatchBroadCastViewModel @Inject constructor(
    private val watchLiveStreamingUseCase: WatchLiveStreamingUseCase<VideoTrack>,
    private val stopViewingUseCase: StopViewingUseCase<VideoTrack>
) : ViewModel() {

    private val _videoTrackState = MutableStateFlow<VideoTrackState>(VideoTrackState.OnLoading)
    val videoTrackState: StateFlow<VideoTrackState> = _videoTrackState

    fun startViewing(broadcastId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            watchLiveStreamingUseCase(broadcastId).collect { result ->
                result.onSuccess { videoTrack ->
                    _videoTrackState.value = VideoTrackState.OnSuccess(videoTrack)
                }.onFailure {
                    _videoTrackState.value = VideoTrackState.OnFailure
                }
            }
        }
    }

    fun stopViewing(surfaceViewRenderer: SurfaceViewRenderer) {
        if (_videoTrackState.value is VideoTrackState.OnSuccess) {
            (_videoTrackState.value as VideoTrackState.OnSuccess).videoTrack.removeSink(surfaceViewRenderer)
            (_videoTrackState.value as VideoTrackState.OnSuccess).videoTrack.dispose()

            viewModelScope.launch(Dispatchers.IO) {
                // WebRTC relaese
                stopViewingUseCase()
            }
        } else {
            _videoTrackState.value = VideoTrackState.OnFailure
        }
    }

    fun addVideoTrackSink(surfaceViewRenderer: SurfaceViewRenderer) {
        if (_videoTrackState.value is VideoTrackState.OnSuccess) {
            (_videoTrackState.value as VideoTrackState.OnSuccess).videoTrack.addSink(surfaceViewRenderer)
        } else {
            _videoTrackState.value = VideoTrackState.OnFailure
        }
    }
}

sealed interface VideoTrackState {
    data class OnSuccess(val videoTrack: VideoTrack) : VideoTrackState
    data object OnFailure : VideoTrackState
    data object OnLoading : VideoTrackState
}