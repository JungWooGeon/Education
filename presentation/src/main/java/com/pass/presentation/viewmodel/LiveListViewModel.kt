package com.pass.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.pass.domain.model.LiveStreaming
import com.pass.domain.usecase.GetUserProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import javax.annotation.concurrent.Immutable
import javax.inject.Inject

@HiltViewModel
class LiveListViewModel @Inject constructor(
    private val getUserProfileUseCase: GetUserProfileUseCase
) : ViewModel(), ContainerHost<LiveListState, LiveListSideEffect> {

    override val container: Container<LiveListState, LiveListSideEffect> = container(
        initialState = LiveListState()
    )

    fun getLiveList() = intent {
        // 로그인 정보 확인
        getUserProfileUseCase().collect { result ->
            result.onSuccess {
                reduce {
                    state.copy(
                        isLoggedIn = true
                    )
                }
            }.onFailure {
                reduce {
                    state.copy(
                        isLoggedIn = false
                    )
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
}

@Immutable
data class LiveListState(
    val isLoggedIn: Boolean = false,
    val liveStreamingList: List<LiveStreaming> = emptyList()
)

sealed interface LiveListSideEffect {
    data object StartLiveStreamingActivity : LiveListSideEffect
    data object NavigateLogInScreen  : LiveListSideEffect
}