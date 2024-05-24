package com.pass.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.pass.domain.model.Video
import dagger.hilt.android.lifecycle.HiltViewModel
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import javax.annotation.concurrent.Immutable
import javax.inject.Inject

@HiltViewModel
class VideoStreamingViewModel @Inject constructor(

) : ViewModel(), ContainerHost<VideoStreamingState, VideoStreamingSideEffect> {

    override val container: Container<VideoStreamingState, VideoStreamingSideEffect> = container (
        initialState = VideoStreamingState()
    )

    fun initContent(video: Video) = intent {
        //TODO 프로필 조회 + video 상태 반영
    }

    fun onChangeMiniPlayer() = intent {
        reduce {
            state.copy(
                isFullScreen = false
            )
        }
    }

    fun onChangeFullScreenPlayer() = intent {
        reduce {
            state.copy(
                isFullScreen = true
            )
        }
    }
}

@Immutable
data class VideoStreamingState(
    val isFullScreen: Boolean = true,

    val videoTitle: String = "",
    val videoUrl: String = "",
    val userProfileURL: String = "",
    val userName: String = ""
)

sealed interface VideoStreamingSideEffect {
    data class Toast(val message: String) : VideoStreamingSideEffect
}