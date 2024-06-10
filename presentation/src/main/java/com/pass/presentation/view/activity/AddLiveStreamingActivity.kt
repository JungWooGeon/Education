package com.pass.presentation.view.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.pass.presentation.ui.theme.MyApplicationTheme
import com.pass.presentation.view.screen.AddLiveStreamingScreen
import dagger.hilt.android.AndroidEntryPoint
import org.webrtc.EglBase
import javax.inject.Inject

@AndroidEntryPoint
class AddLiveStreamingActivity : ComponentActivity() {

    @Inject
    lateinit var eglBaseContext: EglBase.Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AddLiveStreamingScreen(eglBaseContext = eglBaseContext)
                }
            }
        }
    }
}