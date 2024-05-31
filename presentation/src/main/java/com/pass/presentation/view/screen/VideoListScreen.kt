package com.pass.presentation.view.screen

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import com.pass.domain.model.Video
import com.pass.presentation.view.component.VideoListItem
import com.pass.presentation.viewmodel.VideoListSideEffect
import com.pass.presentation.viewmodel.VideoListViewModel
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun VideoListScreen(
    viewModel: VideoListViewModel = hiltViewModel(),
    showVideoStreamingPlayer: (Video) -> Unit
) {
    val videoListState = viewModel.collectAsState().value
    val context = LocalContext.current

    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycleState by lifecycleOwner.lifecycle.currentStateFlow.collectAsState()

    LaunchedEffect(lifecycleState) {
        // onResume 생명 주기 감지 시 동영상 목록 업데이트
        if (lifecycleState == Lifecycle.State.RESUMED) {
            viewModel.readVideoList()
        }
    }

    viewModel.collectSideEffect { sideEffect ->
        when(sideEffect) {
            is VideoListSideEffect.Toast -> Toast.makeText(context, sideEffect.message, Toast.LENGTH_SHORT).show()
        }
    }

    VideoListScreen(
        context = context,
        videoList = videoListState.videoList,
        onClickVideoItem = showVideoStreamingPlayer
    )
}

@Composable
fun VideoListScreen(
    context: Context,
    videoList: List<Video>,
    onClickVideoItem: (Video) -> Unit,
) {
    LazyColumn {
        itemsIndexed(videoList) { idx, video ->
            VideoListItem(
                context = context,
                videoThumbnailUrl = video.videoThumbnailUrl,
                videoTitle = video.videoTitle,
                userProfileUrl = video.userProfileUrl ?: "",
                userName = video.userName ?: "",
                agoTime = video.agoTime,
                isMoreIconButton = false,
                onClickVideoItem = { onClickVideoItem(video) },
                onClickVideoDeleteMoreIcon = { }
            )
        }
    }
}