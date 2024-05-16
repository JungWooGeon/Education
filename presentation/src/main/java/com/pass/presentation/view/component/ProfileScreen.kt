package com.pass.presentation.view.component

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.pass.presentation.ui.theme.MyApplicationTheme
import com.pass.presentation.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(viewModel: ProfileViewModel = hiltViewModel()) {


    ProfileScreen()
}

@Composable
fun ProfileScreen() {
    Column() {

    }
}

@Preview
@Composable
fun PreviewProfileScreen() {
    Surface {
        MyApplicationTheme {
            ProfileScreen()
        }
    }
}