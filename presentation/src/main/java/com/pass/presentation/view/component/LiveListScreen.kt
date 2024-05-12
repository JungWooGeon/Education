package com.pass.presentation.view.component

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.pass.presentation.ui.theme.MyApplicationTheme
import com.pass.presentation.viewmodel.LiveListViewModel

@Composable
fun LiveListScreen(viewModel: LiveListViewModel = hiltViewModel()) {
    LiveListScreen(hiltViewModel())
}

@Composable
fun LiveListScreen() {

}

@Preview
@Composable
fun PreviewLiveListScreen() {
    MyApplicationTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            LiveListScreen()
        }
    }
}