package com.pass.presentation.viewmodel

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.pass.domain.usecase.GetUserProfileUseCase
import com.pass.domain.usecase.StartLiveStreamingUseCase
import com.pass.domain.usecase.StopLiveStreamingUseCase
import com.pass.domain.util.URLCodec
import com.pass.presentation.intent.AddLiveStreamingIntent
import com.pass.presentation.sideeffect.AddLiveStreamingSideEffect
import com.pass.presentation.state.screen.AddLiveStreamingState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import org.webrtc.VideoTrack
import javax.inject.Inject

@HiltViewModel
class AddLiveStreamingViewModel @Inject constructor(
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val startLiveStreamingUseCase: StartLiveStreamingUseCase<VideoTrack, Bitmap>,
    private val stopLiveStreamingUseCase: StopLiveStreamingUseCase<VideoTrack, Bitmap>,
    private val urlCodec: URLCodec<Uri>
) : ViewModel(), ContainerHost<AddLiveStreamingState, AddLiveStreamingSideEffect> {

    override val container: Container<AddLiveStreamingState, AddLiveStreamingSideEffect> = container (
        initialState = AddLiveStreamingState()
    )

    init {
        getUserProfile()
    }

    val startStreamingScope = CoroutineScope(Dispatchers.IO)

    fun processIntent(intent: AddLiveStreamingIntent) {
        when(intent) {
            is AddLiveStreamingIntent.OnClickStartLiveStreamingButton -> {
                onClickStartLiveStreamingButton(intent.thumbnailImage, intent.title)
            }
            is AddLiveStreamingIntent.OnClickBackButtonDuringLiveStreaming -> onClickBackButtonDuringLiveStreaming()
            is AddLiveStreamingIntent.OnDismissRequest -> onDismissRequest()
            is AddLiveStreamingIntent.OnExitRequest -> onExitRequest()
        }
    }

    private fun getUserProfile() = intent {
        getUserProfileUseCase().collect { result ->
            result.onSuccess { profile ->
                reduce {
                    state.copy(
                        userProfileUrl = urlCodec.urlDecode(profile.pictureUrl),
                        profileName = profile.name
                    )
                }
            }.onFailure {
                postSideEffect(AddLiveStreamingSideEffect.FailGetUserProfile)
            }
        }
    }

    private fun onClickStartLiveStreamingButton(thumbnailImage: Bitmap?, title: String) = intent {
        if (thumbnailImage == null) {
            postSideEffect(AddLiveStreamingSideEffect.FailCamera("라이브 방송 시작에 실패하였습니다. 잠시 후 다시 시도해주세요."))
        } else {
            startStreamingScope.launch {
                startLiveStreamingUseCase(
                    title = title,
                    thumbnailImage = thumbnailImage
                ).collect { result ->
                    result.onSuccess { videoTrack ->
                        reduce {
                            state.copy(
                                isLiveStreaming = true,
                                videoTrack = videoTrack
                            )
                        }

                        delay(1000)
                        postSideEffect(AddLiveStreamingSideEffect.SuccessStartLiveStreaming)
                    }.onFailure { e ->
                        postSideEffect(AddLiveStreamingSideEffect.FailCamera(e.message ?: "라이브 방송 시작에 실패하였습니다. 잠시 후 다시 시도해주세요."))
                    }
                }
            }
        }
    }

    private fun onClickBackButtonDuringLiveStreaming() = intent {
        reduce {
            state.copy(isExitDialog = true)
        }
    }

    private fun onDismissRequest() = intent {
        reduce {
            state.copy(isExitDialog = false)
        }
    }

    private fun onExitRequest() = intent {
        startStreamingScope.cancel()
        stopLiveStreamingUseCase()
        postSideEffect(AddLiveStreamingSideEffect.SuccessStopLiveStreaming)
    }
}