package com.pass.presentation.view.component

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.pass.presentation.R
import com.pass.presentation.viewmodel.ProfileSideEffect
import com.pass.presentation.viewmodel.ProfileViewModel
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

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

    ProfileScreen(
        userProfileUrl = profileState.userProfileURL,
        userName = profileState.userName,
        onEditDialog = profileState.onEditDialog,
        editDialogUserName = profileState.editDialogUserName,
        onClickSignOut = viewModel::onClickSignOut,
        onClickEditButton = viewModel::onClickEditButton,
        onCancelEditPopUp = viewModel::onCancelEditPopUp,
        onClickProfileImage = {
            // 갤러리
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
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
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
                    modifier = Modifier
                        .size(60.dp)
                        .clickable {
                            onClickProfileImage()
                        }
                )
            }

            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start,
                modifier = Modifier.padding(start = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = userName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 10.dp)
                    )

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

                Text(
                    text = "Sign Out",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { onClickSignOut() }
                )
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