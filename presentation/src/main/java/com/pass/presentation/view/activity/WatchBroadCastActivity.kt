package com.pass.presentation.view.activity

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.pass.presentation.ui.theme.MyApplicationTheme
import com.pass.presentation.view.screen.WatchBroadCastScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WatchBroadCastActivity: ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val broadcastId = intent.getStringExtra("broadCastId")

        if (broadcastId == null) {
            Toast.makeText(this, "방송 정보가 없습니다.", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            setContent {
                MyApplicationTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        WatchBroadCastScreen(
                            broadcastId = broadcastId
                        )
                    }
                }
            }
        }
    }
}