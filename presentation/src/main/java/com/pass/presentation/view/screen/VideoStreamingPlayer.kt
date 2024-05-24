package com.pass.presentation.view.screen

import android.widget.Toast
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import com.pass.domain.model.Video
import com.pass.presentation.viewmodel.VideoStreamingSideEffect
import com.pass.presentation.viewmodel.VideoStreamingViewModel
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun VideoStreamingPlayer(
    viewModel: VideoStreamingViewModel = hiltViewModel(),
    video: Video,
    paddingValues: PaddingValues,
    onCloseVideoPlayer: () -> Unit
) {
    val videoStreamingState = viewModel.collectAsState().value
    val context = LocalContext.current

    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycleState by lifecycleOwner.lifecycle.currentStateFlow.collectAsState()

    LaunchedEffect(lifecycleState) {
        // onResume 생명 주기 감지 시 비디오 상태 업데이트
        if (lifecycleState == Lifecycle.State.RESUMED) {
            viewModel.initContent(video)
        }
    }

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is VideoStreamingSideEffect.Toast -> Toast.makeText(
                context,
                sideEffect.message,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    VideoStreamingPlayer(
        isFullScreen = videoStreamingState.isFullScreen,
        videoTitle = videoStreamingState.videoTitle,
        videoUrl = videoStreamingState.videoUrl,
        userProfileURL = videoStreamingState.userProfileURL,
        userName = videoStreamingState.userName,
        paddingValues = paddingValues,
        onChangeMiniPlayer = viewModel::onChangeMiniPlayer,
        onChangeFullScreenPlayer = viewModel::onChangeFullScreenPlayer,
        onCloseVideoPlayer = onCloseVideoPlayer
    )
}

@Composable
fun VideoStreamingPlayer(
    isFullScreen: Boolean,
    videoTitle: String,
    videoUrl: String,
    userProfileURL: String,
    userName: String,
    paddingValues: PaddingValues,
    onChangeMiniPlayer: () -> Unit,
    onChangeFullScreenPlayer: () -> Unit,
    onCloseVideoPlayer: () -> Unit
) {
    var isMinimized by remember { mutableStateOf(false) }
    val transition = updateTransition(targetState = isMinimized, label = "Transition")

    val fullScreenWidthDp = LocalConfiguration.current.screenWidthDp.dp
    val fullScreenHeightDp = LocalConfiguration.current.screenHeightDp.dp

    val videoPlayerOffsetY by transition.animateDp(
        transitionSpec = {
            spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessLow
            )
        },
        label = "OffsetY"
    ) { state ->
        if (state) fullScreenHeightDp - paddingValues.calculateBottomPadding() * 2 - paddingValues.calculateTopPadding() else 0.dp
    }

    val videoPlayerScale by transition.animateFloat(
        transitionSpec = {
            spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessLow
            )
        },
        label = "Scale"
    ) { state ->
        if (state) 0.3f else 1f
    }

    val fullScreenContentAlpha by transition.animateFloat(
        transitionSpec = {
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        },
        label = "FullScreenContentAlpha"
    ) { state ->
        if (!state) 1f else 0f
    }

    val miniScreenContentAlpha by transition.animateFloat(
        transitionSpec = {
            spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessLow
            )
        },
        label = "MiniScreenContentAlpha"
    ) { state ->
        if (state) 1f else 0f
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, dragAmount ->
                    if (dragAmount > 0) {
                        isMinimized = true
                    } else if (dragAmount < 0) {
                        isMinimized = false
                    }
                }
            }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // 동영상 플레이어
            Box(
                modifier = Modifier
                    .offset { IntOffset(0, videoPlayerOffsetY.roundToPx()) }
                    .fillMaxWidth(videoPlayerScale)
                    .height(fullScreenWidthDp * videoPlayerScale * 0.66f)
                    .background(Color.Black)
                    .zIndex(1f)
            )

            if (!isMinimized) {
                // 전체 화면에서 보이는 동영상 제목 및 프로필 정보
                Column(
                    modifier = Modifier
                        .padding(
                            top = (fullScreenWidthDp * videoPlayerScale * 0.66f) + 16.dp,
                            start = 16.dp,
                            end = 16.dp,
                            bottom = 16.dp
                        )
                        .alpha(fullScreenContentAlpha)
                ) {
                    Text(text = "Video Title", fontSize = 20.sp)
                    Text(text = "Profile Information", fontSize = 16.sp)
                }
            } else {
                // 미니 플레이어에서 보이는 동영상 제목 및 'X' 아이콘
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .background(Color.White)
                        .padding(
                            start = LocalConfiguration.current.screenWidthDp.dp * videoPlayerScale + 12.dp,
                            bottom = 16.dp,
                            end = 16.dp,
                            top = 16.dp
                        )
                        .alpha(miniScreenContentAlpha)
                ) {
                    Column {
                        Text(
                            text = "동영상 제목",
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = "프로필 제목", fontSize = 12.sp,
                            fontWeight = FontWeight.Light,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))

                    IconButton(onClick = { /* 닫기 동작 */ }) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Close",
                            tint = Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.size(12.dp))

                    IconButton(onClick = onCloseVideoPlayer) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}