package com.pass.presentation.view.screen

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.pass.presentation.view.component.ExitDialog
import com.pass.presentation.viewmodel.VideoTrackState
import com.pass.presentation.viewmodel.WatchBroadCastSideEffect
import com.pass.presentation.viewmodel.WatchBroadCastViewModel
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import org.webrtc.EglBase
import org.webrtc.SurfaceViewRenderer
import org.webrtc.VideoTrack

@Composable
fun WatchBroadCastScreen(
    viewModel: WatchBroadCastViewModel = hiltViewModel(),
    broadcastId: String
) {

    val watchBroadCastState = viewModel.collectAsState().value
    val loadingVideoTrackState = viewModel.videoTrackState.collectAsState()
    val context = LocalContext.current

    // 뒤로 가기 이벤트 - 라이브 스트리밍 중에는 종료 다이얼로그 띄우기
    BackHandler {
        viewModel.onClickBackButton()
    }

    // 첫 시작 시 방송 보기 요청
    LaunchedEffect(Unit) {
        viewModel.startViewing(broadcastId)
    }

    viewModel.collectSideEffect { sideEffect ->
        when(sideEffect) {
            WatchBroadCastSideEffect.SuccessStopLiveStreaming -> (context as Activity).finish()
        }
    }

    when (loadingVideoTrackState.value) {
        is VideoTrackState.OnLoading -> {
            Box(modifier = Modifier.fillMaxSize()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }

        is VideoTrackState.OnSuccess -> {
            val surfaceViewRenderer = remember {
                SurfaceViewRenderer(context).apply {
                    init(EglBase.create().eglBaseContext, null)
                    viewModel.addVideoTrackSink(this)
                }
            }

            // Video Track release
            DisposableEffect(surfaceViewRenderer) {
                onDispose {
                    viewModel.stopViewing(surfaceViewRenderer)
                    surfaceViewRenderer.release()
                }
            }

            WatchBroadCastScreen((loadingVideoTrackState.value as VideoTrackState.OnSuccess).videoTrack)
        }

        is VideoTrackState.OnFailure -> {
            Toast.makeText(context, "방송이 종료되었습니다.", Toast.LENGTH_SHORT).show()
            (context as Activity).finish()
        }
    }

    if (watchBroadCastState.isExitDialog) {
        ExitDialog(
            exitTitle = "방송 시청을 종료하시겠습니까?",
            onDismissRequest = viewModel::onDismissRequest,
            onExitRequest = viewModel::onExitRequest
        )
    }
}

@Composable
fun WatchBroadCastScreen(
    videoTrack: VideoTrack
) {
    AndroidView(
        factory = { context ->
            SurfaceViewRenderer(context).apply {
                init(EglBase.create().eglBaseContext, null)
                videoTrack.addSink(this)
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}