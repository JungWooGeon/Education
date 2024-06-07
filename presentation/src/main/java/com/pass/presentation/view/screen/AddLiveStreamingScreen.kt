package com.pass.presentation.view.screen

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.LifecycleOwner
import com.google.common.util.concurrent.ListenableFuture
import com.pass.presentation.view.component.ExitDialog
import com.pass.presentation.view.component.ProfileImageView
import com.pass.presentation.view.component.SignInInputTextField
import com.pass.presentation.viewmodel.AddLiveStreamingSideEffect
import com.pass.presentation.viewmodel.AddLiveStreamingViewModel
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun AddLiveStreamingScreen(viewModel: AddLiveStreamingViewModel = hiltViewModel()) {

    val addLiveStreamingState = viewModel.collectAsState().value
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycleState by lifecycleOwner.lifecycle.currentStateFlow.collectAsState()

    // CameraX
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val cameraProvider = remember(cameraProviderFuture) { cameraProviderFuture.get() }

    DisposableEffect(lifecycleState) {
        onDispose {
            // CameraX + WebRTC socket release
            cameraProvider.unbindAll()
            viewModel.onStopLiveStreaming()
        }
    }

    // 뒤로 가기 이벤트 - 라이브 스트리밍 중에는 종료 다이얼로그 띄우기
    BackHandler(enabled = addLiveStreamingState.isLiveStreaming) {
        viewModel.onClickBackButtonDuringLiveStreaming()
    }

    viewModel.collectSideEffect { sideEffect ->
        when(sideEffect) {
            AddLiveStreamingSideEffect.FailGetUserProfile -> {
                Toast.makeText(context, "사용자 정보 조회에 실패하였습니다. 다시 로그인해주세요.", Toast.LENGTH_SHORT).show()
                (context as Activity).finish()
            }

            is AddLiveStreamingSideEffect.FailCamera -> {
                Toast.makeText(context, sideEffect.errorMessage, Toast.LENGTH_SHORT).show()
                (context as Activity).finish()
            }

            AddLiveStreamingSideEffect.SuccessStartLiveStreaming -> {
                Toast.makeText(context, "라이브 방송을 시작합니다.", Toast.LENGTH_SHORT).show()
            }

            AddLiveStreamingSideEffect.SuccessStopLiveStreaming -> {
                Toast.makeText(context, "라이브 방송이 종료되었습니다.", Toast.LENGTH_SHORT).show()
                (context as Activity).finish()
            }

            AddLiveStreamingSideEffect.StopCameraX -> {
                cameraProvider.unbindAll()
            }
        }
    }

    AddLiveStreamingScreen(
        cameraProviderFuture = cameraProviderFuture,
        cameraProvider = cameraProvider,
        isLiveStreaming = addLiveStreamingState.isLiveStreaming,
        context = context,
        userProfileUrl = addLiveStreamingState.userProfileUrl,
        liveStreamingTitle = addLiveStreamingState.liveStreamingTitle,
        lifecycleOwner = lifecycleOwner,
        onFailCamera = viewModel::onFailCamera,
        onChangeLiveStreamingTitle = viewModel::onChangeLiveStreamingTitle,
        onClickStartLiveStreamingButton = viewModel::onClickStartLiveStreamingButton
    )

    if (addLiveStreamingState.isExitDialog) {
        ExitDialog(
            exitTitle = "라이브 스트리밍을 종료하시겠습니까?",
            onDismissRequest = viewModel::onDismissRequest,
            onExitRequest = viewModel::onExitRequest
        )
    }
}

@Composable
fun AddLiveStreamingScreen(
    cameraProviderFuture: ListenableFuture<ProcessCameraProvider>,
    cameraProvider: ProcessCameraProvider,
    isLiveStreaming: Boolean,
    context: Context,
    userProfileUrl: String,
    liveStreamingTitle: String,
    lifecycleOwner: LifecycleOwner,
    onFailCamera: (String) -> Unit,
    onChangeLiveStreamingTitle: (String) -> Unit,
    onClickStartLiveStreamingButton: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)

                cameraProviderFuture.addListener({
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview
                        )
                    } catch (exc: Exception) {
                        onFailCamera("CameraX Use case binding failed $exc")
                    }

                    preview.setSurfaceProvider(previewView.surfaceProvider)
                }, ContextCompat.getMainExecutor(ctx))

                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        if (!isLiveStreaming) {
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
            Box(modifier = Modifier
                .clip(shape = CircleShape)
                .padding(top = 30.dp)
                .size(40.dp)
                .background(Color.Red)
                .align(Alignment.TopCenter))
        }
    }
}