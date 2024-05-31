package com.pass.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.pass.domain.model.Video
import com.pass.domain.usecase.GetAllVideoListUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
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
    val getAllVideoListUseCase: GetAllVideoListUseCase
) : ViewModel(), ContainerHost<VideoListState, VideoListSideEffect> {

    override val container: Container<VideoListState, VideoListSideEffect> = container(
        initialState = VideoListState()
    )

    fun readVideoList() = intent {
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
}

@Immutable
data class VideoListState(
    val videoList: List<Video> = emptyList()
)

sealed interface VideoListSideEffect {
    data class Toast(val message: String) : VideoListSideEffect
}