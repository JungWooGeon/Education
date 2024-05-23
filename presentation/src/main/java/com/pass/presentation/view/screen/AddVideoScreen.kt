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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pass.presentation.view.component.ExoPlayerView
import com.pass.presentation.viewmodel.AddVideoSideEffect
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
        viewModel.createVideoThumbnail(videoUri)
    }

    AddVideoScreen(
        context = context,
        videoUri = addVideoState.videoUri,
        title = addVideoState.title,
        onChangeTitle = viewModel::onChangeTitle,
        onClickUploadButton = viewModel::onClickUploadButton,
        progressButtonState = addVideoState.progressButtonState
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddVideoScreen(
    context: Context,
    videoUri: String,
    title: String,
    onChangeTitle: (String) -> Unit,
    onClickUploadButton: () -> Unit,
    progressButtonState: SSButtonState
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
                    .clip(RoundedCornerShape(10.dp))
            )

            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(15.dp),
                value = title,
                onValueChange = onChangeTitle,
                placeholder = {
                    Text(text = "동영상 제목 추가")
                },
                shape = RoundedCornerShape(8.dp),
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                )
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