package com.pass.presentation.viewmodel

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.lifecycle.ViewModel
import com.pass.domain.usecase.AddVideoUseCase
import com.pass.domain.usecase.CreateVideoThumbnailUseCase
import com.simform.ssjetpackcomposeprogressbuttonlibrary.SSButtonState
import dagger.hilt.android.lifecycle.HiltViewModel
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import java.io.ByteArrayOutputStream
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import javax.annotation.concurrent.Immutable
import javax.inject.Inject

@HiltViewModel
class AddVideoViewModel @Inject constructor(
    private val createVideoThumbnailUseCase: CreateVideoThumbnailUseCase,
    private val addVideoUseCase: AddVideoUseCase
) : ViewModel(), ContainerHost<AddVideoState, AddVideoSideEffect> {

    override val container: Container<AddVideoState, AddVideoSideEffect> = container(
        initialState = AddVideoState(null, "", "", SSButtonState.IDLE)
    )

    fun createVideoThumbnail(videoUri: String) = intent {
        // uri 상태 복사
        reduce {
            state.copy(videoUri = URLDecoder.decode(videoUri, StandardCharsets.UTF_8.toString()))
        }

        // thumbnail 생성 후 상태 저장
        val result = createVideoThumbnailUseCase(videoUri)

        result.onSuccess { bitmapString ->
            reduce {
                state.copy(videoThumbnailBitmap = convertStringToBitmap(bitmapString))
            }
        }.onFailure { e ->
            postSideEffect(AddVideoSideEffect.Toast(e.message ?: "동영상 선택을 취소하였습니다."))
        }
    }

    fun onChangeTitle(title: String) = intent {
        reduce {
            state.copy(title = title)
        }
    }

    fun onClickUploadButton() = intent {
        reduce {
            state.copy(
                progressButtonState = SSButtonState.LOADING
            )
        }

        if (state.videoThumbnailBitmap == null) {
            postSideEffect(AddVideoSideEffect.Toast("동영상 업로드에 실패하였습니다. 다시 시도해주세요."))
        } else {
            addVideoUseCase(
                videoUri = state.videoUri,
                videoThumbnailBitmap = convertBitmapToString(state.videoThumbnailBitmap!!),
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

    private fun convertStringToBitmap(base64Str: String): Bitmap? {
        val decodedBytes = Base64.decode(base64Str, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    }

    private fun convertBitmapToString(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }
}

@Immutable
data class AddVideoState(
    val videoThumbnailBitmap: Bitmap?,
    val videoUri: String,
    val title: String,
    val progressButtonState: SSButtonState
)

sealed interface AddVideoSideEffect {
    data class Toast(val message: String) : AddVideoSideEffect
    data object NavigateProfileScreen : AddVideoSideEffect
}