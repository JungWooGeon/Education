package com.pass.presentation.view.component

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.pass.presentation.viewmodel.LiveListViewModel

@Composable
fun LiveListScreen(viewModel: LiveListViewModel = hiltViewModel()) {
    LiveListScreen(hiltViewModel())
}

@Composable
fun LiveListScreen() {
    Text(text = "live list screen")
}