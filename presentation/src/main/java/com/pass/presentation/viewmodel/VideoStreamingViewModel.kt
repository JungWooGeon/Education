package com.pass.presentation.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.pass.domain.model.Video
import com.pass.domain.usecase.GetOtherUserProfileUseCase
import com.pass.domain.util.URLCodec
import com.pass.presentation.intent.VideoStreamingIntent
import com.pass.presentation.sideeffect.VideoStreamingSideEffect
import com.pass.presentation.state.screen.VideoStreamingState
import dagger.hilt.android.lifecycle.HiltViewModel
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class VideoStreamingViewModel @Inject constructor(
    private val getOtherUserProfileUseCase: GetOtherUserProfileUseCase,
    private val urlCodec: URLCodec<Uri>
) : ViewModel(), ContainerHost<VideoStreamingState, VideoStreamingSideEffect> {

    override val container: Container<VideoStreamingState, VideoStreamingSideEffect> = container(
        initialState = VideoStreamingState()
    )

    fun processIntent(intent: VideoStreamingIntent) {
        when(intent) {
            is VideoStreamingIntent.InitContent -> initContent(intent.video)
            is VideoStreamingIntent.OnChangeMiniPlayer -> onChangeMiniPlayer()
            is VideoStreamingIntent.OnChangeFullScreenPlayer -> onChangeFullScreenPlayer()
        }
    }

    private fun initContent(video: Video) = intent {
        // video 사용자 프로필 조회
        getOtherUserProfileUseCase(video.userId).collect { result ->
            result.onSuccess { profile ->
                // 프로필 조회 성공 시 video 상태 반영
                reduce {
                    state.copy(
                        isMinimized = false,
                        videoTitle = video.videoTitle,
                        videoUrl = urlCodec.urlDecode(video.videoUrl),
                        userProfileURL = urlCodec.urlDecode(profile.pictureUrl),
                        userName = profile.name
                    )
                }
            }.onFailure {
                // 프로필 조회 실패
                postSideEffect(VideoStreamingSideEffect.FailVideoStreaming(it.message ?: "프로필 조회에 실패하였습니다."))
            }
        }
    }

    private fun onChangeMiniPlayer() = intent {
        reduce {
            state.copy(
                isMinimized = true
            )
        }
    }

    private fun onChangeFullScreenPlayer() = intent {
        reduce {
            state.copy(
                isMinimized = false
            )
        }
    }
}