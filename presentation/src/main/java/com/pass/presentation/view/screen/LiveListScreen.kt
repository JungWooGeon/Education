package com.pass.presentation.view.screen

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import coil.compose.AsyncImage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.pass.domain.model.LiveStreaming
import com.pass.presentation.view.activity.AddLiveStreamingActivity
import com.pass.presentation.viewmodel.LiveListSideEffect
import com.pass.presentation.viewmodel.LiveListViewModel
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LiveListScreen(
    viewModel: LiveListViewModel = hiltViewModel(),
    onNavigateLogInScreen: () -> Unit
) {

    val liveListState = viewModel.collectAsState().value
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycleState by lifecycleOwner.lifecycle.currentStateFlow.collectAsState()

    LaunchedEffect(lifecycleState) {
        // onResume 생명 주기 감지 시 로그인 상태 확인 후 리스트 표시
        if (lifecycleState == Lifecycle.State.RESUMED) {
            viewModel.getLiveList()
        }
    }

    // 권한 허용 - 카메라 권한
    val singlePermissionsState = rememberPermissionState(Manifest.permission.CAMERA)

    viewModel.collectSideEffect { sideEffect ->
        when(sideEffect) {
            LiveListSideEffect.StartLiveStreamingActivity -> {
                if (singlePermissionsState.status.isGranted) {
                    // 권한 허용 - 화면 실행
                    context.startActivity(Intent(context, AddLiveStreamingActivity::class.java))
                } else if (singlePermissionsState.status.shouldShowRationale) {
                    // 권한 미허용 - 설정으로 이동
                    startSettings(context, "라이브 기능을 사용하려면 카메라 권한이 필요합니다.")
                } else {
                    // 처음일 경우 - 권한 요청
                    singlePermissionsState.launchPermissionRequest()
                }
            }

            LiveListSideEffect.NavigateLogInScreen -> onNavigateLogInScreen()
        }
    }

    LiveListScreen(
        liveStreamingList = liveListState.liveStreamingList,
        onClickStartLiveStreamingButton = viewModel::startLiveStreamingActivity
    )
}

@Composable
fun LiveListScreen(
    liveStreamingList: List<LiveStreaming>,
    onClickStartLiveStreamingButton: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(vertical = 10.dp)) {
            LazyColumn {
                items(liveStreamingList) { liveStreaming ->
                    LiveStreamingItem(
                        thumbnailUrl = liveStreaming.thumbnailURL,
                        title = liveStreaming.title,
                        userProfileUrl = liveStreaming.userProfileURL,
                        userName = liveStreaming.userName
                    )
                }
            }
        }

        SmallFloatingActionButton(
            onClick = onClickStartLiveStreamingButton,
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.secondary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(top = 16.dp, bottom = 64.dp, start = 16.dp, end = 16.dp)
        ) {
            Icon(Icons.Filled.Add, "Small floating action button.")
        }
    }
}

@Composable
fun LiveStreamingItem(
    thumbnailUrl: String,
    title: String,
    userProfileUrl: String,
    userName: String
) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 20.dp)) {
        AsyncImage(
            model = thumbnailUrl,
            contentDescription = title,
            modifier = Modifier.aspectRatio(2f),
            contentScale = ContentScale.FillWidth
        )

        Row(
            modifier = Modifier.padding(top = 10.dp, start = 10.dp, end = 10.dp)
        ) {
            AsyncImage(
                model = userProfileUrl,
                contentDescription = null,
                clipToBounds = true,
                modifier = Modifier.size(40.dp)
            )

            Column(
                modifier = Modifier.padding(start = 10.dp)
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )

                Text(
                    text = userName,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 5.dp)
                )
            }
        }
    }
}

private fun startSettings(context: Context, toastMessage: String) {
    Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show()
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(Uri.parse("package:${context.packageName}"))
    context.startActivity(intent)
}