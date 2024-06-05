package com.pass.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.pass.domain.usecase.GetUserProfileUseCase
import com.pass.domain.usecase.StartLiveStreamingUseCase
import com.pass.domain.usecase.StopLiveStreamingUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import org.webrtc.VideoTrack
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import javax.annotation.concurrent.Immutable
import javax.inject.Inject

@HiltViewModel
class AddLiveStreamingViewModel @Inject constructor(
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val startLiveStreamingUseCase: StartLiveStreamingUseCase<VideoTrack>,
    private val stopListStreamingUseCase: StopLiveStreamingUseCase<VideoTrack>
) : ViewModel(), ContainerHost<AddLiveStreamingState, AddLiveStreamingSideEffect> {

    override val container: Container<AddLiveStreamingState, AddLiveStreamingSideEffect> = container (
        initialState = AddLiveStreamingState()
    )

    init {
        getUserProfile()
    }

    private fun getUserProfile() = intent {
        getUserProfileUseCase().collect { result ->
            result.onSuccess { profile ->
                reduce {
                    state.copy(
                        userProfileUrl = URLDecoder.decode(profile.pictureUrl, StandardCharsets.UTF_8.toString()),
                        liveStreamingTitle = "${profile.name}의 방송을 시작합니다."
                    )
                }
            }.onFailure {
                postSideEffect(AddLiveStreamingSideEffect.FailGetUserProfile)
            }
        }
    }

    fun onChangeLiveStreamingTitle(title: String) = intent {
        reduce {
            state.copy(
                liveStreamingTitle = title
            )
        }
    }

    fun onFailCamera(errorMessage: String) = intent {
        postSideEffect(AddLiveStreamingSideEffect.FailCamera(errorMessage))
    }

    fun onClickStartLiveStreamingButton() = intent {
        startLiveStreamingUseCase(state.liveStreamingTitle).collect { result ->
            result.onSuccess {
                reduce {
                    state.copy(isLiveStreaming = true)
                }
                postSideEffect(AddLiveStreamingSideEffect.SuccessStartLiveStreaming)
            }.onFailure { e ->
                postSideEffect(AddLiveStreamingSideEffect.FailCamera(e.message ?: "라이브 방송 시작에 실패하였습니다. 잠시 후 다시 시도해주세요."))
            }
        }
    }

    fun onClickBackButtonDuringLiveStreaming() = intent {
        reduce {
            state.copy(isExitDialog = true)
        }
    }

    fun onDismissRequest() = intent {
        reduce {
            state.copy(isExitDialog = false)
        }
    }

    fun onExitRequest() = intent {
        stopListStreamingUseCase()
        postSideEffect(AddLiveStreamingSideEffect.SuccessStopLiveStreaming)
    }

    // TODO lifecyle 종료 시 camerax release + webrtc release(with usecase)
    // TODO 라이브 스트리밍 종료 + 뒤로 가기 이벤트 막기
}

@Immutable
data class AddLiveStreamingState(
    val userProfileUrl: String = "",
    val liveStreamingTitle: String = "",
    val isLiveStreaming: Boolean = false,
    val isExitDialog: Boolean = false
)

sealed interface AddLiveStreamingSideEffect {
    data object FailGetUserProfile : AddLiveStreamingSideEffect
    data class FailCamera(val errorMessage: String) : AddLiveStreamingSideEffect
    data object SuccessStartLiveStreaming : AddLiveStreamingSideEffect
    data object SuccessStopLiveStreaming : AddLiveStreamingSideEffect
}