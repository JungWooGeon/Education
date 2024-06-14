package com.pass.presentation.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import com.pass.domain.usecase.AddVideoUseCase
import com.pass.domain.usecase.CreateVideoThumbnailUseCase
import com.pass.domain.util.BitmapConverter
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
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import javax.inject.Inject

@HiltViewModel
class AddVideoViewModel @Inject constructor(
    private val createVideoThumbnailUseCase: CreateVideoThumbnailUseCase,
    private val addVideoUseCase: AddVideoUseCase,
    private val bitmapConverter: BitmapConverter<Bitmap>
) : ViewModel(), ContainerHost<AddVideoState, AddVideoSideEffect> {

    override val container: Container<AddVideoState, AddVideoSideEffect> = container(
        initialState = AddVideoState(null, "", "", SSButtonState.IDLE)
    )

    fun processIntent(intent: AddVideoIntent) {
        when(intent) {
            is AddVideoIntent.CreateVideoThumbnail -> createVideoThumbnail(intent.videoUri)
            is AddVideoIntent.OnChangeTitle -> onChangeTitle(intent.title)
            is AddVideoIntent.OnClickUploadButton -> onClickUploadButton()
        }
    }

    private fun createVideoThumbnail(videoUri: String) = intent {
        // uri 상태 복사
        reduce {
            state.copy(videoUri = URLDecoder.decode(videoUri, StandardCharsets.UTF_8.toString()))
        }

        // thumbnail 생성 후 상태 저장
        val result = createVideoThumbnailUseCase(videoUri)

        result.onSuccess { bitmapString ->
            reduce {
                state.copy(videoThumbnailBitmap = bitmapConverter.convertStringToBitmap(bitmapString))
            }
        }.onFailure { e ->
            postSideEffect(AddVideoSideEffect.Toast(e.message ?: "동영상 선택을 취소하였습니다."))
        }
    }

    private fun onChangeTitle(title: String) = intent {
        reduce {
            state.copy(title = title)
        }
    }

    private fun onClickUploadButton() = intent {
        reduce {
            state.copy(
                progressButtonState = SSButtonState.LOADING
            )
        }

        if (state.videoThumbnailBitmap == null) {
            postSideEffect(AddVideoSideEffect.Toast("동영상 업로드에 실패하였습니다. 다시 시도해주세요."))
            reduce {
                state.copy(
                    progressButtonState = SSButtonState.FAILURE
                )
            }
        } else if (state.title == "") {
            postSideEffect(AddVideoSideEffect.Toast("제목을 입력해주세요."))
            reduce {
                state.copy(
                    progressButtonState = SSButtonState.FAILURE
                )
            }
        } else {
            addVideoUseCase(
                videoUri = state.videoUri,
                videoThumbnailBitmap = bitmapConverter.convertBitmapToString(state.videoThumbnailBitmap!!),
                title = state.title
            ).collect { result ->
                result.onSuccess {
                    reduce {
                        state.copy(
                            progressButtonState = SSButtonState.SUCCESS
                        )
                    }
                    postSideEffect(AddVideoSideEffect.Toast("동영상을 업로드하였습니다."))
                    postSideEffect(AddVideoSideEffect.NavigateProfileScreen)
                }.onFailure { e ->
                    reduce {
                        state.copy(
                            progressButtonState = SSButtonState.FAILURE
                        )
                    }
                    postSideEffect(AddVideoSideEffect.Toast(e.message ?: "동영상 업로드에 실패하였습니다. 다시 시도해주세요."))
                }
            }
        }
    }
}