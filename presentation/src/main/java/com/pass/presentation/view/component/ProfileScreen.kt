package com.pass.presentation.view.component

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
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
        when(sideEffect) {
            is ProfileSideEffect.Toast -> Toast.makeText(context, sideEffect.message, Toast.LENGTH_SHORT).show()
            is ProfileSideEffect.NavigateSignInScreen -> {
                onNavigateToSignInScreen()
                Toast.makeText(context, "로그아웃을 완료하였습니다.",Toast.LENGTH_SHORT).show()
            }
        }
    }

    ProfileScreen(
        userProfileURL = "https://upload.wikimedia.org/wikipedia/commons/thumb/e/ea/Android_logo_2023_%28stacked%29.svg/242px-Android_logo_2023_%28stacked%29.svg.png",
        userName = profileState.userName,
        onClickSignOut = viewModel::onClickSignOut
    )
}

@Composable
fun ProfileScreen(
    userProfileURL: String,
    userName: String,
    onClickSignOut: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        AsyncImage(
            model = userProfileURL,
            contentDescription = "",
            modifier = Modifier.aspectRatio(2f),
            contentScale = ContentScale.FillWidth
        )

        Row(

        ){
            Column {
                Text(
                    modifier = Modifier.padding(bottom = 10.dp),
                    text = userName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Button(
                    modifier = Modifier.padding(top = 20.dp),
                    contentPadding = PaddingValues(16.dp),
                    onClick = onClickSignOut
                ) {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = "Sign Out",
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }


}