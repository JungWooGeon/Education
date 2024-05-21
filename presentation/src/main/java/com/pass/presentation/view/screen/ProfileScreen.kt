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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.pass.domain.model.Video
import com.pass.presentation.R
import com.pass.presentation.view.component.EditNameDialog
import com.pass.presentation.view.component.ProfileImage
import com.pass.presentation.view.component.ProfileInfoBox
import com.pass.presentation.viewmodel.ProfileSideEffect
import com.pass.presentation.viewmodel.ProfileViewModel
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onNavigateToSignInScreen: () -> Unit
) {
    val profileState = viewModel.collectAsState().value
    val context = LocalContext.current

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is ProfileSideEffect.Toast -> Toast.makeText(
                context,
                sideEffect.message,
                Toast.LENGTH_SHORT
            ).show()

            is ProfileSideEffect.NavigateSignInScreen -> {
                onNavigateToSignInScreen()
                Toast.makeText(context, "로그아웃을 완료하였습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 프로필 사진 선택 런처
    val singlePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> viewModel.onChangeUserProfilePicture(uri) }
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
        onEditDialog = profileState.onEditDialog,
        editDialogUserName = profileState.editDialogUserName,
        onClickSignOut = viewModel::onClickSignOut,
        onClickEditButton = viewModel::onClickEditButton,
        onCancelEditPopUp = viewModel::onCancelEditPopUp,
        onClickProfileImage = {
            if (multiplePermissionsState.allPermissionsGranted) {
                // 권한 허용
                singlePhotoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            } else if (multiplePermissionsState.shouldShowRationale) {
                // 권한 미허용
                Toast.makeText(context, "프로필 기능을 사용하려면 마이크 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
                val intent =
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(Uri.parse("package:${context.packageName}"))
                context.startActivity(intent)
            } else {
                // 처음일 경우 - 권한 요청
                multiplePermissionsState.launchMultiplePermissionRequest()
            }
        },
        onChangeEditDialogUserName = viewModel::onChangeEditDialogUserName,
        onClickSaveEditDialogButton = viewModel::onClickSaveEditDialogButton
    )
}

@Composable
fun ProfileScreen(
    context: Context,
    userProfileUrl: String,
    userName: String,
    onEditDialog: Boolean,
    editDialogUserName: String,
    onClickSignOut: () -> Unit,
    onClickEditButton: () -> Unit,
    onCancelEditPopUp: () -> Unit,
    onClickProfileImage: () -> Unit,
    onChangeEditDialogUserName: (String) -> Unit,
    onClickSaveEditDialogButton: () -> Unit
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

            val testVideoList = listOf(
                Video(
                    userId = "",
                    videoThumbnailUrl = userProfileUrl,
                    videoTitle = "테스트 제목 입니다. 많은 시청 바랍니다.",
                    time = "1년전"
                ),
                Video(
                    userId = "",
                    videoThumbnailUrl = userProfileUrl,
                    videoTitle = "시작",
                    time = "10년전"
                ),
                Video(
                    userId = "",
                    videoThumbnailUrl = "",
                    videoTitle = "테스트 제목 입니다. 많은 시청 바랍니다. 지금 바로 시작합니다!",
                    time = "1년전"
                ),
                Video(
                    userId = "",
                    videoThumbnailUrl = userProfileUrl,
                    videoTitle = "테스트 제목 입니다. 많은 시청 바랍니다.",
                    time = "1년전"
                ),
                Video(
                    userId = "",
                    videoThumbnailUrl = userProfileUrl,
                    videoTitle = "시작",
                    time = "10년전"
                ),
                Video(
                    userId = "",
                    videoThumbnailUrl = "",
                    videoTitle = "테스트 제목 입니다. 많은 시청 바랍니다. 지금 바로 시작합니다!",
                    time = "1년전"
                )
            )

            LazyColumn {
                items(testVideoList) { video ->
                    VideoListItem(
                        context = context,
                        videoThumbnailUrl = video.videoThumbnailUrl,
                        videoTitle = video.videoTitle,
                        userProfileUrl = userProfileUrl,
                        userName = userName,
                        onClickVideoItem = { }
                    )
                }
            }
        }

        SmallFloatingActionButton(
            onClick = { },
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
}

@Composable
fun VideoListItem(
    context: Context,
    videoThumbnailUrl: String,
    videoTitle: String,
    userProfileUrl: String,
    userName: String,
    onClickVideoItem: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(10.dp)
            .clickable { onClickVideoItem() }
    ) {
        // video thumbnail image
        if (videoThumbnailUrl == "") {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1.5f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White)
            )
        } else {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(videoThumbnailUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                clipToBounds = true,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1.5f)
                    .clip(RoundedCornerShape(10.dp))
            )
        }

        // user profile / video title
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
                .padding(start = 10.dp, top = 10.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxHeight()
            ) {
                Row(
                    modifier = Modifier.fillMaxHeight()
                ) {
                    ProfileImage(
                        context = context,
                        modifier = Modifier.padding(4.dp),
                        userProfileUrl = userProfileUrl,
                        imageSize = 16.dp,
                        onClickProfileImage = {}
                    )

                    Text(
                        text = userName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        modifier = Modifier.padding(start = 5.dp)
                    )
                }

                Text(
                    text = videoTitle,
                    fontSize = 12.sp,
                    lineHeight = 20.sp,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 2,
                    modifier = Modifier.padding(top = 5.dp)
                )

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "7년 전",
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 5.dp)
                )
            }
        }

        // video delete Icon Button
        IconButton(
            onClick = {
                // @TODO Bottom Sheet
            }
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_more_vertical),
                contentDescription = null,
                tint = Color.Black.copy(alpha = 0.8F),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}