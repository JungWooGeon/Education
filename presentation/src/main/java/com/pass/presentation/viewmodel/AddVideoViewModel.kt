package com.pass.presentation.viewmodel

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.lifecycle.ViewModel
import com.pass.domain.usecase.CreateVideoThumbnailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import javax.annotation.concurrent.Immutable
import javax.inject.Inject

@HiltViewModel
class AddVideoViewModel @Inject constructor(
    val createVideoThumbnailUseCase: CreateVideoThumbnailUseCase
) : ViewModel(), ContainerHost<AddVideoState, AddVideoSideEffect> {

    override val container: Container<AddVideoState, AddVideoSideEffect> = container(
        initialState = AddVideoState(null, "", "")
    )

    fun createVideoThumbnail(videoUri: String) = intent {
        println(withContext(Dispatchers.IO) {
            URLDecoder.decode(videoUri, StandardCharsets.UTF_8.toString())
        })
        // uri 상태 복사
        reduce {
            state.copy(videoUri = URLDecoder.decode(videoUri, StandardCharsets.UTF_8.toString()))
        }

        // thumbnail 생성 후 상태 저장
        val result = createVideoThumbnailUseCase(videoUri)

        result.onSuccess { bitmapString ->
            reduce {
                state.copy(videoThumbnailBitmap = base64ToBitmap(bitmapString))
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

    }

    private fun base64ToBitmap(base64Str: String): Bitmap? {
        val decodedBytes = Base64.decode(base64Str, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    }
}

@Immutable
data class AddVideoState(
    val videoThumbnailBitmap: Bitmap?,
    val videoUri: String,
    val title: String
)

sealed interface AddVideoSideEffect {
    data class Toast(val message: String) : AddVideoSideEffect
    data object NavigateProfileScreen : AddVideoSideEffect
}