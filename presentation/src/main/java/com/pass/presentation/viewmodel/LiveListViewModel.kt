package com.pass.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pass.domain.model.LiveStreaming
import com.pass.domain.usecase.GetLiveStreamingListUseCase
import com.pass.domain.usecase.IsSignedInUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
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
class LiveListViewModel @Inject constructor(
    private val isSignedInUseCase: IsSignedInUseCase,
    private val getLiveStreamingListUseCase: GetLiveStreamingListUseCase<VideoTrack>
) : ViewModel(), ContainerHost<LiveListState, LiveListSideEffect> {

    override val container: Container<LiveListState, LiveListSideEffect> = container(
        initialState = LiveListState()
    )

    fun getLiveList() = intent {
        viewModelScope.launch {
            // 라이브 리스트 정보 확인
            getLiveStreamingListUseCase().collect { result ->
                result.onSuccess { liveStreamingList ->
                    reduce {
                        state.copy(liveStreamingList = liveStreamingList)
                    }
                }.onFailure {
                    postSideEffect(LiveListSideEffect.Toast(it.message ?: "라이브 중인 방송이 없습니다."))
                }
            }
        }

        viewModelScope.launch {
            // 로그인 정보 확인
            isSignedInUseCase().collect { result ->
                reduce {
                    state.copy(isLoggedIn = result)
                }
            }
        }
    }

    fun startLiveStreamingActivity() = intent {
        if (state.isLoggedIn) {
            postSideEffect(LiveListSideEffect.StartLiveStreamingActivity)
        } else {
            postSideEffect(LiveListSideEffect.NavigateLogInScreen)
        }
    }

    fun onClickLiveStreamingItem(broadcastId: String) = intent {
        postSideEffect(LiveListSideEffect.StartWatchBroadCastActivity(broadcastId))
    }
}

@Immutable
data class LiveListState(
    val isLoggedIn: Boolean = false,
    val liveStreamingList: List<LiveStreaming> = emptyList()
)

sealed interface LiveListSideEffect {
    data object StartLiveStreamingActivity : LiveListSideEffect
    data object NavigateLogInScreen : LiveListSideEffect
    data class Toast(val message: String) : LiveListSideEffect
    data class StartWatchBroadCastActivity(val broadcastId: String) : LiveListSideEffect
}