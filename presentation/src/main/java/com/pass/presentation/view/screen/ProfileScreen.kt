package com.pass.presentation.view.screen

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.READ_MEDIA_IMAGES
import android.Manifest.permission.READ_MEDIA_VIDEO
import android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.pass.domain.model.Video
import com.pass.presentation.view.component.DeleteModalBottomSheet
import com.pass.presentation.view.component.EditNameDialog
import com.pass.presentation.view.component.ProfileInfoBox
import com.pass.presentation.view.component.VideoListItem
import com.pass.presentation.viewmodel.ProfileSideEffect
import com.pass.presentation.viewmodel.ProfileViewModel
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onNavigateToSignInScreen: () -> Unit,
    onNavigateToAddVideoScreen: (String) -> Unit,
    onClickVideoItem: (Video) -> Unit
) {
    val profileState = viewModel.collectAsState().value
    val context = LocalContext.current

    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycleState by lifecycleOwner.lifecycle.currentStateFlow.collectAsState()

    LaunchedEffect(lifecycleState) {
        // onResume 생명 주기 감지 시 동영상 목록 업데이트
        // 프로필 사진과 이름은 변경 시 자동으로 UI 업데이트 되지만, 동영상은 추가 화면으로 이동 후 다시 돌아오기 때문
        if (lifecycleState == Lifecycle.State.RESUMED) {
            viewModel.readProfile()
        }
    }

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is ProfileSideEffect.Toast -> Toast.makeText(context, sideEffect.message, Toast.LENGTH_SHORT).show()

            is ProfileSideEffect.NavigateSignInScreen -> {
                onNavigateToSignInScreen()
                Toast.makeText(context, "로그아웃을 완료하였습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 프로필 사진 선택 런처
    val singlePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> uri?.let{ viewModel.onChangeUserProfilePicture(uri) } }
    )

    // 동영상 사진 선택 런처
    val singleVideoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> uri?.let{ onNavigateToAddVideoScreen(URLEncoder.encode(uri.toString(), StandardCharsets.UTF_8.toString())) } }
    )

    // 권한 허용 - OS 버전에 따른 이미지 읽기 권한 목록
    val multiplePermissionsState = rememberMultiplePermissionsState(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            listOf(READ_MEDIA_IMAGES, READ_MEDIA_VIDEO, READ_MEDIA_VISUAL_USER_SELECTED)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            listOf(READ_MEDIA_IMAGES, READ_MEDIA_VIDEO)
        } else {
            listOf(READ_EXTERNAL_STORAGE)
        }
    )

    ProfileScreen(
        context = context,
        userProfileUrl = profileState.userProfileURL,
        userName = profileState.userName,
        videoList = profileState.videoList,
        onEditDialog = profileState.onEditDialog,
        editDialogUserName = profileState.editDialogUserName,
        onClickSignOut = viewModel::onClickSignOut,
        onClickEditButton = viewModel::onClickEditButton,
        onCancelEditPopUp = viewModel::onCancelEditPopUp,
        isOpenDeleteModalBottomSheet = profileState.isOpenDeleteModalBottomSheet,
        onClickProfileImage = {
            if (multiplePermissionsState.allPermissionsGranted) {
                // 권한 허용 - 사진 선택
                singlePhotoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            } else if (multiplePermissionsState.shouldShowRationale) {
                // 권한 미허용 - 설정으로 이동
                startSettings(context, "프로필 기능을 사용하려면 사진/동영상 권한이 필요합니다.")
            } else {
                // 처음일 경우 - 권한 요청
                multiplePermissionsState.launchMultiplePermissionRequest()
            }
        },
        onChangeEditDialogUserName = viewModel::onChangeEditDialogUserName,
        onClickSaveEditDialogButton = viewModel::onClickSaveEditDialogButton,
        onClickAddVideoButton = {
            if (multiplePermissionsState.allPermissionsGranted) {
                // 권한 허용 - 비디오 선택
                singleVideoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
            } else if (multiplePermissionsState.shouldShowRationale) {
                // 권한 미허용 - 설정으로 이동
                startSettings(context, "동영상 기능을 사용하려면 사진/동영상 권한이 필요합니다.")
            } else {
                // 처음일 경우 - 권한 요청
                multiplePermissionsState.launchMultiplePermissionRequest()
            }
        },
        closeDeleteModalBottomSheet = viewModel::closeDeleteModalBottomSheet,
        openDeleteModalBottomSheet = viewModel::openDeleteModalBottomSheet,
        deleteVideoItem = viewModel::deleteVideoItem,
        onClickVideoItem = onClickVideoItem
    )
}

@Composable
fun ProfileScreen(
    context: Context,
    userProfileUrl: String,
    userName: String,
    videoList: List<Video>,
    onEditDialog: Boolean,
    editDialogUserName: String,
    isOpenDeleteModalBottomSheet: Boolean,
    onClickSignOut: () -> Unit,
    onClickEditButton: () -> Unit,
    onCancelEditPopUp: () -> Unit,
    onClickProfileImage: () -> Unit,
    onChangeEditDialogUserName: (String) -> Unit,
    onClickSaveEditDialogButton: () -> Unit,
    onClickAddVideoButton: () -> Unit,
    closeDeleteModalBottomSheet: () -> Unit,
    openDeleteModalBottomSheet: (Int) -> Unit,
    deleteVideoItem: () -> Unit,
    onClickVideoItem: (Video) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            ProfileInfoBox(
                context = context,
                userProfileUrl = userProfileUrl,
                userName = userName,
                onClickProfileImage = onClickProfileImage,
                onClickSignOut = onClickSignOut,
                onClickEditButton = onClickEditButton
            )

            Text(
                text = "동영상",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 20.dp, start = 20.dp, bottom = 20.dp)
            )

            LazyColumn {
                itemsIndexed(videoList) { idx, video ->
                    VideoListItem(
                        context = context,
                        videoThumbnailUrl = video.videoThumbnailUrl,
                        videoTitle = video.videoTitle,
                        userProfileUrl = userProfileUrl,
                        userName = userName,
                        onClickVideoItem = { onClickVideoItem(video) },
                        onClickVideoDeleteMoreIcon = { openDeleteModalBottomSheet(idx) }
                    )
                }
            }
        }

        SmallFloatingActionButton(
            onClick = onClickAddVideoButton,
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.secondary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Filled.Add, "Small floating action button.")
        }
    }

    if (onEditDialog) {
        EditNameDialog(
            onDismissRequest = onCancelEditPopUp,
            textFieldValue = editDialogUserName,
            onValueChange = onChangeEditDialogUserName,
            onClickSaveEditDialogButton = onClickSaveEditDialogButton
        )
    }

    if (isOpenDeleteModalBottomSheet) {
        DeleteModalBottomSheet(
            closeBottomSheet = closeDeleteModalBottomSheet,
            onClickDeleteItem = deleteVideoItem
        )
    }
}

private fun startSettings(context: Context, toastMessage: String) {
    Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show()
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(Uri.parse("package:${context.packageName}"))
    context.startActivity(intent)
}