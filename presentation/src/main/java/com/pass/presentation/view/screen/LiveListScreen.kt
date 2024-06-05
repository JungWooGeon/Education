package com.pass.presentation.view.screen

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.pass.domain.model.LiveStreaming
import com.pass.presentation.view.activity.AddLiveStreamingActivity
import com.pass.presentation.view.activity.WatchBroadCastActivity
import com.pass.presentation.view.component.LiveStreamingItem
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
    val multiplePermissionsState = rememberMultiplePermissionsState(listOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO))

    viewModel.collectSideEffect { sideEffect ->
        when(sideEffect) {
            is LiveListSideEffect.StartLiveStreamingActivity -> {
                if (multiplePermissionsState.allPermissionsGranted) {
                    // 권한 허용 - 화면 실행
                    context.startActivity(Intent(context, AddLiveStreamingActivity::class.java))
                } else if (multiplePermissionsState.shouldShowRationale) {
                    // 권한 미허용 - 설정으로 이동
                    startSettings(context, "라이브 기능을 사용하려면 카메라 및 오디오 권한이 필요합니다.")
                } else {
                    // 처음일 경우 - 권한 요청
                    multiplePermissionsState.launchMultiplePermissionRequest()
                }
            }

            is LiveListSideEffect.NavigateLogInScreen -> onNavigateLogInScreen()
            is LiveListSideEffect.Toast -> { Toast.makeText(context, sideEffect.message, Toast.LENGTH_SHORT).show() }
            is LiveListSideEffect.StartWatchBroadCastActivity -> {
                val intent = Intent(context, WatchBroadCastActivity::class.java)
                intent.putExtra("broadCastId", sideEffect.broadcastId)
                context.startActivity(Intent(context, WatchBroadCastActivity::class.java))
            }
        }
    }

    LiveListScreen(
        liveStreamingList = liveListState.liveStreamingList,
        onClickStartLiveStreamingButton = viewModel::startLiveStreamingActivity,
        onClickLiveStreamingItem = viewModel::onClickLiveStreamingItem
    )
}

@Composable
fun LiveListScreen(
    liveStreamingList: List<LiveStreaming>,
    onClickStartLiveStreamingButton: () -> Unit,
    onClickLiveStreamingItem: (String) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (liveStreamingList.isEmpty()) {
            Text(
                text = "라이브 목록 없음",
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            Column(modifier = Modifier.padding(vertical = 10.dp)) {
                LazyColumn {
                    items(liveStreamingList) { liveStreaming ->
                        LiveStreamingItem(
                            thumbnailUrl = liveStreaming.thumbnailURL,
                            title = liveStreaming.title,
                            userProfileUrl = liveStreaming.userProfileURL,
                            userName = liveStreaming.userName,
                            onClickLiveStreamingItem = {
                                onClickLiveStreamingItem(liveStreaming.broadcastId)
                            }
                        )
                    }
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

private fun startSettings(context: Context, toastMessage: String) {
    Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show()
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(Uri.parse("package:${context.packageName}"))
    context.startActivity(intent)
}