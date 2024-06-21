package com.pass.presentation.viewmodel

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.pass.domain.usecase.AddVideoUseCase
import com.pass.domain.usecase.CreateVideoThumbnailUseCase
import com.pass.domain.util.URLCodec
import com.pass.presentation.intent.AddVideoIntent
import com.pass.presentation.sideeffect.AddVideoSideEffect
import com.pass.presentation.state.screen.AddVideoState
import com.simform.ssjetpackcomposeprogressbuttonlibrary.SSButtonState
import dagger.hilt.android.lifecycle.HiltViewModel
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class AddVideoViewModel @Inject constructor(
    private val createVideoThumbnailUseCase: CreateVideoThumbnailUseCase<Bitmap>,
    private val addVideoUseCase: AddVideoUseCase<Bitmap>,
    private val urlCodec: URLCodec<Uri>
) : ViewModel(), ContainerHost<AddVideoState, AddVideoSideEffect> {

    override val container: Container<AddVideoState, AddVideoSideEffect> = container(
        initialState = AddVideoState(null, "", SSButtonState.IDLE)
    )

    fun processIntent(intent: AddVideoIntent) {
        when(intent) {
            is AddVideoIntent.CreateVideoThumbnail -> createVideoThumbnail(intent.videoUri)
            is AddVideoIntent.OnClickUploadButton -> onClickUploadButton(intent.title)
        }
    }

    private fun createVideoThumbnail(videoUri: String) = intent {
        // uri 상태 복사
        reduce {
            state.copy(videoUri = urlCodec.urlDecode(videoUri))
        }

        // thumbnail 생성 후 상태 저장
        val result = createVideoThumbnailUseCase(videoUri)

        result.onSuccess { bitmap ->
            reduce {
                state.copy(videoThumbnailBitmap = bitmap)
            }
        }.onFailure { e ->
            postSideEffect(AddVideoSideEffect.Toast(e.message ?: "동영상 선택을 취소하였습니다."))
        }
    }

    private fun onClickUploadButton(title: String) = intent {
        reduce {
            state.copy(progressButtonState = SSButtonState.LOADING)
        }

        if (state.videoThumbnailBitmap == null) {
            postSideEffect(AddVideoSideEffect.Toast("동영상 업로드에 실패하였습니다. 다시 시도해주세요."))
            reduce {
                state.copy(progressButtonState = SSButtonState.FAILURE)
            }
        } else if (title == "") {
            postSideEffect(AddVideoSideEffect.Toast("제목을 입력해주세요."))
            reduce {
                state.copy(progressButtonState = SSButtonState.FAILURE)
            }
        } else {
            addVideoUseCase(
                videoUri = state.videoUri,
                videoThumbnailBitmap = state.videoThumbnailBitmap!!,
                title = title
            ).collect { result ->
                result.onSuccess {
                    reduce {
                        state.copy(progressButtonState = SSButtonState.SUCCESS)
                    }
                    postSideEffect(AddVideoSideEffect.Toast("동영상을 업로드하였습니다."))
                    postSideEffect(AddVideoSideEffect.NavigateProfileScreen)
                }.onFailure { e ->
                    reduce {
                        state.copy(progressButtonState = SSButtonState.FAILURE)
                    }
                    postSideEffect(AddVideoSideEffect.Toast(e.message ?: "동영상 업로드에 실패하였습니다. 다시 시도해주세요."))
                }
            }
        }
    }
}