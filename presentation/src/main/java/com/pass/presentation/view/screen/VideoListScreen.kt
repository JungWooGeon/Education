package com.pass.presentation.view.screen

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.pass.presentation.viewmodel.VideoListViewModel

@Composable
fun VideoListScreen(viewModel: VideoListViewModel = hiltViewModel()) {
    VideoListScreen()
}

@Composable
fun VideoListScreen() {
    Text(text = "video screen")
}