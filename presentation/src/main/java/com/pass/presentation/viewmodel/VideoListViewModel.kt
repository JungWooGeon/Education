package com.pass.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pass.domain.model.Video
import com.pass.domain.usecase.GetAllVideoListUseCase
import com.pass.domain.usecase.IsSignedInUseCase
import com.pass.presentation.intent.VideoListIntent
import com.pass.presentation.sideeffect.VideoListSideEffect
import com.pass.presentation.state.screen.VideoListState
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
import javax.inject.Inject

@HiltViewModel
class VideoListViewModel @Inject constructor(
    private val getAllVideoListUseCase: GetAllVideoListUseCase,
    private val isSignedInUseCase: IsSignedInUseCase
) : ViewModel(), ContainerHost<VideoListState, VideoListSideEffect> {

    override val container: Container<VideoListState, VideoListSideEffect> = container(
        initialState = VideoListState()
    )

    fun processIntent(intent: VideoListIntent) {
        when(intent) {
            is VideoListIntent.ReadVideoList -> readVideoList()
            is VideoListIntent.OnClickVideoItem -> onClickVideoItem(intent.video)
        }
    }

    private fun readVideoList() = intent {

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

    private fun onClickVideoItem(video: Video) = intent {
        if (state.isLoggedIn) {
            postSideEffect(VideoListSideEffect.ShowVideoStreamingPlayer(video))
        } else {
            postSideEffect(VideoListSideEffect.Toast("로그인이 필요합니다."))
            postSideEffect(VideoListSideEffect.NavigateLogInScreen)
        }
    }
}