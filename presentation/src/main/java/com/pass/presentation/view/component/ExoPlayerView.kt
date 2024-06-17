package com.pass.presentation.view.component

import android.content.Context
import android.net.Uri
import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView

@OptIn(UnstableApi::class)
@Composable
fun ExoPlayerView(
    context: Context,
    videoUri: String,
    modifier: Modifier = Modifier,
    isChange: Boolean = false,
    playBackPosition: Long = 0L,
    onChangeIsFullScreen: (Long) -> Unit,
    updatePlaybackPosition: ((Long) -> Unit)? = null
) {
    val currentIsChange = rememberUpdatedState(isChange)

    // Initialize ExoPlayer
    val exoPlayer = remember { ExoPlayer.Builder(context).build() }

    // 임의로 seekbar 변경 시마다 현재 시점 백업
    exoPlayer.addListener(object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            if (events.contains(Player.EVENT_POSITION_DISCONTINUITY) || events.contains(Player.EVENT_PLAYBACK_STATE_CHANGED)) {
                updatePlaybackPosition?.invoke(player.currentPosition)
            }
        }
    })

    // MediaSource를 생성하기 위한 DataSource.Factory 인스턴스 준비
    val dataSourceFactory = DefaultDataSource.Factory(context)

    // Create a MediaSource
    val mediaSource = remember(videoUri) {
        ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(
                MediaItem.fromUri(Uri.parse(videoUri))
            )
    }

    // Uri 변경 시마다 Exoplayer 셋팅
    LaunchedEffect(mediaSource) {
        exoPlayer.setMediaSource(mediaSource)
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
        exoPlayer.seekTo(playBackPosition)
    }

    // 생명주기 종료 시 exoplayer 메모리 해제
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    // Exoplayer with AndroidView
    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = exoPlayer
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
                setFullscreenButtonClickListener {
                    if (currentIsChange.value) {
                        // 변경 가능한 설정일 때, 현재 시점 정보와 함께 전체 화면으로 전환
                        onChangeIsFullScreen((player as ExoPlayer).currentPosition)
                    }
                }
            }
        },
        modifier = modifier
    )
}