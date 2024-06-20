package com.pass.presentation.view.component

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Build
import android.view.View
import android.view.WindowInsets
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import com.pass.domain.model.Video
import com.pass.presentation.intent.VideoStreamingIntent
import com.pass.presentation.sideeffect.VideoStreamingSideEffect
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

    // 뒤로가기 클릭 이벤트
    BackHandler(enabled = !videoStreamingState.isMinimized) {
        if (videoStreamingState.isFullScreen) {
            // 전체 플레이어 일 때, 기본 플레이어로 전환
            viewModel.processIntent(VideoStreamingIntent.OnChangeIsFullScreen(null))
        } else {
            // 기본 플레이어 일 때, 미니 플레이어로 전환
            viewModel.processIntent(VideoStreamingIntent.OnChangeMiniPlayer)
        }
    }

    // 비디오 상태 업데이트 (초기)
    LaunchedEffect(Unit) {
        viewModel.processIntent(VideoStreamingIntent.InitContent(video))
    }

    viewModel.collectSideEffect { sideEffect ->
        when(sideEffect) {
            is VideoStreamingSideEffect.FailVideoStreaming -> {
                Toast.makeText(context, sideEffect.errorMessage, Toast.LENGTH_SHORT).show()
                (context as Activity).finish()
            }
        }
    }

    VideoStreamingPlayer(
        context = context,
        isMinimized = videoStreamingState.isMinimized,
        videoTitle = videoStreamingState.videoTitle,
        videoUrl = videoStreamingState.videoUrl,
        userProfileURL = videoStreamingState.userProfileURL,
        userName = videoStreamingState.userName,
        paddingValues = paddingValues,
        isFullScreenState = videoStreamingState.isFullScreen,
        playBackPosition = videoStreamingState.currentPosition,
        onChangeMiniPlayer = { viewModel.processIntent(VideoStreamingIntent.OnChangeMiniPlayer) },
        onChangeDefaultPlayer = { viewModel.processIntent(VideoStreamingIntent.OnChangeDefaultPlayer) },
        onCloseVideoPlayer = {
            viewModel.processIntent(VideoStreamingIntent.UpdatePlaybackPosition(0L))
            onCloseVideoPlayer()
        },
        onChangeIsFullScreen = { viewModel.processIntent(VideoStreamingIntent.OnChangeIsFullScreen(it)) },
        updatePlaybackPosition = { viewModel.processIntent(VideoStreamingIntent.UpdatePlaybackPosition(it)) }
    )
}

@Composable
fun VideoStreamingPlayer(
    context: Context,
    isMinimized: Boolean,
    videoTitle: String,
    videoUrl: String,
    userProfileURL: String,
    userName: String,
    paddingValues: PaddingValues,
    isFullScreenState: Boolean,
    playBackPosition: Long,
    onChangeMiniPlayer: () -> Unit,
    onChangeDefaultPlayer: () -> Unit,
    onCloseVideoPlayer: () -> Unit,
    onChangeIsFullScreen: (Long) -> Unit,
    updatePlaybackPosition: (Long) -> Unit
) {
    val transition = updateTransition(targetState = isMinimized, label = "Transition")

    // 전체 스크린 dp 정의
    val fullScreenWidthDp = LocalConfiguration.current.screenWidthDp.dp
    val fullScreenHeightDp = LocalConfiguration.current.screenHeightDp.dp

    // 비디오 플레이어의 Y 좌표 정의 - paddingValues : MainScreen의 Scaffold paddingValues
    val videoPlayerOffsetY by transition.animateDp(
        transitionSpec = { spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow) },
        label = "OffsetY"
    ) { state -> if (state) fullScreenHeightDp - paddingValues.calculateBottomPadding() - paddingValues.calculateTopPadding() else 0.dp }

    // 비디오 플레이어의 크기 정의
    val videoPlayerScale by transition.animateFloat(
        transitionSpec = { spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow) },
        label = "Scale"
    ) { state -> if (state) 0.3f else 1f }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .zIndex(if (!isMinimized) 1f else 0f)
            .background(MaterialTheme.colorScheme.background)
            .offset { IntOffset(0, videoPlayerOffsetY.roundToPx()) }
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, dragAmount ->
                    if (dragAmount > 0) {
                        if (!isFullScreenState) onChangeMiniPlayer()
                    } else if (dragAmount < 0) {
                        onChangeDefaultPlayer()
                    }
                }
            }
            .clickable { if (isMinimized) onChangeDefaultPlayer() }
    ) {
        if (isFullScreenState) {
            // 전체 화면으로 전환
            (context as Activity).requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

            // 상태 바 숨기기
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                context.window.setDecorFitsSystemWindows(false)
                context.window.insetsController?.hide(WindowInsets.Type.statusBars())
            } else {
                context.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
            }

            // 동영상 플레이어
            ExoPlayerView(
                context = context,
                videoUri = videoUrl,
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                isChange = !isMinimized,
                playBackPosition = playBackPosition,
                onChangeIsFullScreen = onChangeIsFullScreen,
                updatePlaybackPosition = updatePlaybackPosition
            )
        } else {
            // 전체 화면 해제
            (context as Activity).requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED

            // 상태 바 보이기
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                context.window.insetsController?.show(WindowInsets.Type.statusBars())
                context.window.setDecorFitsSystemWindows(true)
            } else {
                context.window.decorView.systemUiVisibility = View.SYSTEM_UI_LAYOUT_FLAGS
            }

            // 동영상 플레이어
            ExoPlayerView(
                context = context,
                videoUri = videoUrl,
                modifier = Modifier
                    .fillMaxWidth(videoPlayerScale)
                    .height(fullScreenWidthDp * videoPlayerScale * 0.66f)
                    .background(Color.Black)
                    .zIndex(1f),
                isChange = !isMinimized,
                playBackPosition = playBackPosition,
                onChangeIsFullScreen = onChangeIsFullScreen,
                updatePlaybackPosition = updatePlaybackPosition
            )

            if (!isMinimized) {
                // 전체 화면에서 보이는 동영상 제목 및 프로필 정보
                Column(
                    modifier = Modifier
                        .padding(
                            top = (fullScreenWidthDp * videoPlayerScale * 0.66f) + 20.dp,
                            start = 16.dp,
                            end = 16.dp,
                            bottom = 16.dp
                        )
                        .fillMaxHeight()
                ) {
                    Text(text = videoTitle, fontSize = 20.sp)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(top = 20.dp)
                    ) {
                        ProfileImageView(
                            context = context,
                            modifier = Modifier.padding(4.dp),
                            userProfileUrl = userProfileURL,
                            imageSize = 40.dp,
                            onClickProfileImage = {}
                        )

                        Text(
                            text = userName,
                            fontSize = 16.sp,
                            lineHeight = 20.sp,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 2,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
            } else {
                // 미니 플레이어에서 보이는 동영상 제목 및 'X' 아이콘
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .background(Color.White)
                        .padding(
                            start = fullScreenWidthDp * videoPlayerScale + 12.dp,
                            bottom = 16.dp,
                            end = 16.dp,
                            top = 16.dp
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 10.dp)
                    ) {
                        Text(
                            text = videoTitle,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = userName,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Light,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    IconButton(onClick = onCloseVideoPlayer) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.Gray,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}