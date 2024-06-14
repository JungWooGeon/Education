package com.pass.presentation.view.screen

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.LifecycleOwner
import com.google.common.util.concurrent.ListenableFuture
import com.pass.presentation.intent.AddLiveStreamingIntent
import com.pass.presentation.sideeffect.AddLiveStreamingSideEffect
import com.pass.presentation.view.component.ExitDialog
import com.pass.presentation.view.component.PreviewCameraX
import com.pass.presentation.view.component.ProfileImageView
import com.pass.presentation.view.component.SignInInputTextField
import com.pass.presentation.viewmodel.AddLiveStreamingViewModel
import io.getstream.webrtc.android.compose.VideoRenderer
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import org.webrtc.EglBase
import org.webrtc.RendererCommon
import org.webrtc.VideoTrack

@Composable
fun AddLiveStreamingScreen(
    viewModel: AddLiveStreamingViewModel = hiltViewModel(),
    eglBaseContext: EglBase.Context
) {

    val addLiveStreamingState = viewModel.collectAsState().value
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // CameraX
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val cameraProvider = remember(cameraProviderFuture) { cameraProviderFuture.get() }

    // 뒤로 가기 이벤트 - 라이브 스트리밍 중에는 종료 다이얼로그 띄우기
    BackHandler(enabled = addLiveStreamingState.isLiveStreaming) {
        viewModel.processIntent(AddLiveStreamingIntent.OnClickBackButtonDuringLiveStreaming)
    }

    viewModel.collectSideEffect { sideEffect ->
        when(sideEffect) {
            is AddLiveStreamingSideEffect.FailGetUserProfile -> {
                Toast.makeText(context, "사용자 정보 조회에 실패하였습니다. 다시 로그인해주세요.", Toast.LENGTH_SHORT).show()
                (context as Activity).finish()
            }

            is AddLiveStreamingSideEffect.FailCamera -> {
                Toast.makeText(context, sideEffect.errorMessage, Toast.LENGTH_SHORT).show()
                (context as Activity).finish()
            }

            is AddLiveStreamingSideEffect.SuccessStartLiveStreaming -> {
                Toast.makeText(context, "라이브 방송을 시작합니다.", Toast.LENGTH_SHORT).show()
            }

            is AddLiveStreamingSideEffect.SuccessStopLiveStreaming -> {
                Toast.makeText(context, "라이브 방송이 종료되었습니다.", Toast.LENGTH_SHORT).show()
                (context as Activity).finish()
            }
        }
    }

    AddLiveStreamingScreen(
        eglBaseContext = eglBaseContext,
        videoTrack = addLiveStreamingState.videoTrack,
        isLiveStreaming = addLiveStreamingState.isLiveStreaming,
        context = context,
        lifecycleOwner = lifecycleOwner,
        cameraProviderFuture = cameraProviderFuture,
        cameraProvider = cameraProvider,
        userProfileUrl = addLiveStreamingState.userProfileUrl,
        liveStreamingTitle = addLiveStreamingState.liveStreamingTitle,
        onChangeLiveStreamingTitle = { viewModel.processIntent(AddLiveStreamingIntent.OnChangeLiveStreamingTitle(it)) },
        onClickStartLiveStreamingButton = {
            cameraProvider.unbindAll()
            viewModel.processIntent(AddLiveStreamingIntent.OnClickStartLiveStreamingButton)
        }
    )

    if (addLiveStreamingState.isExitDialog) {
        ExitDialog(
            exitTitle = "라이브 스트리밍을 종료하시겠습니까?",
            onDismissRequest = { viewModel.processIntent(AddLiveStreamingIntent.OnDismissRequest) },
            onExitRequest = { viewModel.processIntent(AddLiveStreamingIntent.OnExitRequest) }
        )
    }
}

@Composable
fun AddLiveStreamingScreen(
    eglBaseContext: EglBase.Context,
    videoTrack: VideoTrack?,
    isLiveStreaming: Boolean,
    context: Context,
    lifecycleOwner : LifecycleOwner,
    userProfileUrl: String,
    liveStreamingTitle: String,
    cameraProviderFuture: ListenableFuture<ProcessCameraProvider>,
    cameraProvider: ProcessCameraProvider,
    onChangeLiveStreamingTitle: (String) -> Unit,
    onClickStartLiveStreamingButton: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (!isLiveStreaming || videoTrack == null) {
            PreviewCameraX(
                modifier = Modifier.fillMaxSize(),
                lifecycleOwner = lifecycleOwner,
                cameraProviderFuture = cameraProviderFuture,
                cameraProvider = cameraProvider
            )

            Row(
                modifier = Modifier.padding(top = 50.dp, start = 30.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ProfileImageView(
                    context = context,
                    modifier = Modifier.padding(4.dp),
                    userProfileUrl = userProfileUrl,
                    imageSize = 40.dp,
                    onClickProfileImage = {}
                )

                SignInInputTextField(
                    modifier = Modifier.padding(end = 30.dp),
                    value = liveStreamingTitle,
                    onChangeValue = onChangeLiveStreamingTitle,
                    placeHolderValue = "제목을 입력해주세요.",
                    containerColor = Color.Transparent
                )
            }

            Button(
                onClick = onClickStartLiveStreamingButton,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(40.dp)
                    .align(Alignment.BottomCenter)
            ) {
                Text(text = "실시간 라이브 시작하기")
            }
        } else {
            VideoRenderer(
                videoTrack = videoTrack,
                modifier = Modifier.fillMaxSize(),
                eglBaseContext = eglBaseContext,
                rendererEvents = object : RendererCommon.RendererEvents {
                    override fun onFirstFrameRendered() {  }
                    override fun onFrameResolutionChanged(videoWidth: Int, videoHeight: Int, rotation: Int) {  }
                }
            )

            Box(modifier = Modifier
                .clip(shape = CircleShape)
                .padding(top = 30.dp)
                .size(20.dp)
                .background(Color.Red)
                .align(Alignment.TopCenter))
        }
    }
}