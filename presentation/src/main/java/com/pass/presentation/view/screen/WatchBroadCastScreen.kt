package com.pass.presentation.view.screen

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.pass.presentation.intent.WatchBroadCastIntent
import com.pass.presentation.sideeffect.WatchBroadCastSideEffect
import com.pass.presentation.state.loading.VideoTrackState
import com.pass.presentation.view.component.ExitDialog
import com.pass.presentation.viewmodel.WatchBroadCastViewModel
import io.getstream.webrtc.android.compose.VideoRenderer
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import org.webrtc.EglBase
import org.webrtc.RendererCommon
import org.webrtc.VideoTrack

@Composable
fun WatchBroadCastScreen(
    viewModel: WatchBroadCastViewModel = hiltViewModel(),
    broadcastId: String,
    eglBaseContext: EglBase.Context
) {

    val watchBroadCastState = viewModel.collectAsState().value
    val context = LocalContext.current

    // 뒤로 가기 이벤트 - 라이브 스트리밍 중에는 종료 다이얼로그 띄우기
    BackHandler {
        viewModel.processIntent(WatchBroadCastIntent.OnClickBackButton)
    }

    // 첫 시작 시 방송 보기 요청
    LaunchedEffect(Unit) {
        viewModel.processIntent(WatchBroadCastIntent.StartViewing(broadcastId))
    }

    viewModel.collectSideEffect { sideEffect ->
        when(sideEffect) {
            WatchBroadCastSideEffect.SuccessStopLiveStreaming -> (context as Activity).finish()
        }
    }

    when (watchBroadCastState.videoTrackState) {
        is VideoTrackState.OnLoading -> {
            Box(modifier = Modifier.fillMaxSize()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }

        is VideoTrackState.OnSuccess -> {
            WatchBroadCastScreen(
                videoTrack = watchBroadCastState.videoTrackState.videoTrack,
                eglBaseContext = eglBaseContext
            )
        }

        is VideoTrackState.OnFailure -> {
            Toast.makeText(context, "방송이 종료되었습니다.", Toast.LENGTH_SHORT).show()
            (context as Activity).finish()
        }
    }

    if (watchBroadCastState.isExitDialog) {
        ExitDialog(
            exitTitle = "방송 시청을 종료하시겠습니까?",
            onDismissRequest = { viewModel.processIntent(WatchBroadCastIntent.OnDismissRequest) },
            onExitRequest = { viewModel.processIntent(WatchBroadCastIntent.OnExitRequest) }
        )
    }
}

@Composable
fun WatchBroadCastScreen(
    videoTrack: VideoTrack,
    eglBaseContext: EglBase.Context
) {
    Box(modifier = Modifier.fillMaxSize()) {
        VideoRenderer(
            videoTrack = videoTrack,
            modifier = Modifier.fillMaxSize(),
            eglBaseContext = eglBaseContext,
            rendererEvents = object : RendererCommon.RendererEvents {
                override fun onFirstFrameRendered() {  }
                override fun onFrameResolutionChanged(videoWidth: Int, videoHeight: Int, rotation: Int) {  }
            }
        )
    }
}
