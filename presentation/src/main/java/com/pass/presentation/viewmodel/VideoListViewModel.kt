package com.pass.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pass.domain.model.Video
import com.pass.domain.usecase.GetAllVideoListUseCase
import com.pass.domain.usecase.IsSignedInUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
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
class VideoListViewModel @Inject constructor(
    private val getAllVideoListUseCase: GetAllVideoListUseCase,
    private val isSignedInUseCase: IsSignedInUseCase
) : ViewModel(), ContainerHost<VideoListState, VideoListSideEffect> {

    override val container: Container<VideoListState, VideoListSideEffect> = container(
        initialState = VideoListState()
    )

    fun readVideoList() = intent {

        viewModelScope.launch {
            // 비디오 리스트 조회
            getAllVideoListUseCase().collect { result ->
                result.onSuccess { videoList ->
                    val mutableVideoList = mutableListOf<Video>()

                    videoList.forEach {
                        mutableVideoList.add(
                            Video(
                                videoId = it.videoId,
                                userId = it.userId,
                                videoThumbnailUrl = it.videoThumbnailUrl,
                                videoTitle = it.videoTitle,
                                agoTime = it.agoTime,
                                videoUrl = it.videoUrl,
                                userName = it.userName,
                                userProfileUrl = URLDecoder.decode(it.userProfileUrl, StandardCharsets.UTF_8.toString())
                            )
                        )
                    }

                    reduce {
                        state.copy(videoList = mutableVideoList.toList())
                    }
                }.onFailure {
                    postSideEffect(VideoListSideEffect.Toast(it.message ?: "동영상 조회에 실패하였습니다. 잠시 후 다시 시도해주세요."))
                }
            }
        }

        viewModelScope.launch {
            // 로그인 정보 확인
            isSignedInUseCase().collect { result ->
                reduce {
                    state.copy(isLoggedIn = result)
                }
            }
        }
    }

    fun onClickVideoItem(video: Video) = intent {
        if (state.isLoggedIn) {
            postSideEffect(VideoListSideEffect.ShowVideoStreamingPlayer(video))
        } else {
            postSideEffect(VideoListSideEffect.NavigateLogInScreen)
        }
    }
}

@Immutable
data class VideoListState(
    val videoList: List<Video> = emptyList(),
    val isLoggedIn: Boolean = false
)

sealed interface VideoListSideEffect {
    data class Toast(val message: String) : VideoListSideEffect
    data class ShowVideoStreamingPlayer(val video: Video) : VideoListSideEffect
    data object NavigateLogInScreen : VideoListSideEffect
}