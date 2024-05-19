package com.pass.presentation.view.component

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.READ_MEDIA_IMAGES
import android.Manifest.permission.READ_MEDIA_VIDEO
import android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.pass.presentation.R
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
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(Uri.parse("package:${context.packageName}"))
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
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(top = 10.dp, start = 20.dp, end = 10.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.Center
        ) {
            if (userProfileUrl == "") {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(
                            width = 1.dp,
                            shape = CircleShape,
                            color = Color.LightGray
                        )
                        .clickable { onClickProfileImage() }
                )
            } else {
                AsyncImage(
                    model = userProfileUrl,
                    contentDescription = null,
                    clipToBounds = true,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .border(
                            width = 1.dp,
                            shape = CircleShape,
                            color = Color.LightGray
                        )
                        .clickable { onClickProfileImage() }
                )
            }

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.Start,
                    modifier = Modifier.padding(start = 16.dp).weight(1f)
                ) {
                    Text(
                        text = userName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 8.dp, end = 10.dp)
                    )

                    Text(
                        text = "Sign Out",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .padding(top = 10.dp)
                            .clickable { onClickSignOut() }
                    )
                }

                IconButton(
                    onClick = onClickEditButton
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_edit),
                        contentDescription = null,
                        tint = Color.Black.copy(alpha = 0.8F),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }

    if (onEditDialog) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f))  // 배경을 반투명한 검정색으로 설정
                .clickable { onCancelEditPopUp() }
        )

        EditNameDialog(
            onDismissRequest = onCancelEditPopUp,
            textFieldValue = editDialogUserName,
            onValueChange = onChangeEditDialogUserName,
            onClickSaveEditDialogButton = onClickSaveEditDialogButton
        )
    }
}