package com.pass.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pass.domain.usecase.GetUserProfileUseCase
import com.pass.domain.usecase.StartLiveStreamingUseCase
import com.pass.domain.usecase.StopLiveStreamingUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
    private val stopLiveStreamingUseCase: StopLiveStreamingUseCase<VideoTrack>
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
        // CameraX 자원 해제
        postSideEffect(AddLiveStreamingSideEffect.StopCameraX)

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
        postSideEffect(AddLiveStreamingSideEffect.SuccessStopLiveStreaming)
    }

    fun onStopLiveStreaming() {
        viewModelScope.launch(Dispatchers.IO) {
            stopLiveStreamingUseCase()
        }
    }
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
    data object StopCameraX : AddLiveStreamingSideEffect
}