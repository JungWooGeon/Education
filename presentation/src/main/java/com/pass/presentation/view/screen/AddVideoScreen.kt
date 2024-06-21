package com.pass.presentation.view.screen

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pass.presentation.intent.AddVideoIntent
import com.pass.presentation.sideeffect.AddVideoSideEffect
import com.pass.presentation.view.component.CodeBridgeTextField
import com.pass.presentation.view.component.ExoPlayerView
import com.pass.presentation.viewmodel.AddVideoViewModel
import com.simform.ssjetpackcomposeprogressbuttonlibrary.SSButtonState
import com.simform.ssjetpackcomposeprogressbuttonlibrary.SSButtonType
import com.simform.ssjetpackcomposeprogressbuttonlibrary.SSJetPackComposeProgressButton
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun AddVideoScreen(
    viewModel: AddVideoViewModel = hiltViewModel(),
    videoUri: String,
    onNavigateProfileScreen: () -> Unit
) {
    val addVideoState = viewModel.collectAsState().value
    val context = LocalContext.current

    // TextField 한글 자소 분리 현상 완화를 위해 UI 상태로 적용
    var title by remember { mutableStateOf("") }

    viewModel.collectSideEffect { sideEffect ->
        when(sideEffect) {
            is AddVideoSideEffect.Toast -> {
                Toast.makeText(context, sideEffect.message, Toast.LENGTH_SHORT).show()
            }
            is AddVideoSideEffect.NavigateProfileScreen -> {
                onNavigateProfileScreen()
            }
        }
    }

    LaunchedEffect(key1 = videoUri) {
        viewModel.processIntent(AddVideoIntent.CreateVideoThumbnail(videoUri))
    }

    AddVideoScreen(
        context = context,
        videoUri = addVideoState.videoUri,
        title = title,
        onChangeTitle = { title = it },
        onClickUploadButton = { viewModel.processIntent(AddVideoIntent.OnClickUploadButton(title)) },
        progressButtonState = addVideoState.progressButtonState
    )
}

@Composable
fun AddVideoScreen(
    context: Context,
    videoUri: String,
    title: String,
    progressButtonState: SSButtonState,
    onChangeTitle: (String) -> Unit,
    onClickUploadButton: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column {
            ExoPlayerView(
                context = context,
                videoUri = videoUri,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.5f)
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(10.dp)),
                onChangeIsFullScreen = { }
            )

            CodeBridgeTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(15.dp),
                value = title,
                onChangeValue = onChangeTitle,
                placeHolderValue = "동영상 제목 추가",
                containerColor = Color.Transparent
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            SSJetPackComposeProgressButton(
                type = SSButtonType.ZOOM_IN_OUT_CIRCLE,
                width = LocalConfiguration.current.screenWidthDp.dp,
                height = 50.dp,
                assetColor = Color.White,
                colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colorScheme.primary, contentColor = Color.White),
                buttonState = progressButtonState,
                text = "동영상 업로드",
                onClick = onClickUploadButton
            )
        }
    }
}