package com.pass.presentation.view.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.pass.domain.model.LiveStreaming
import com.pass.presentation.ui.theme.MyApplicationTheme
import com.pass.presentation.viewmodel.LiveListViewModel

@Composable
fun LiveListScreen(viewModel: LiveListViewModel = hiltViewModel()) {
    LiveListScreen(
        liveStreamingList = listOf(
            LiveStreaming(
                thumbnailURL = "https://upload.wikimedia.org/wikipedia/commons/thumb/e/ea/Android_logo_2023_%28stacked%29.svg/242px-Android_logo_2023_%28stacked%29.svg.png",
                title = "안녕하세요. 방송 시작합니다.",
                content = "오늘만 이루어지는 하아리이트 방송 시작하겠습니다. 모두 모두 모여주세요."
            ),
            LiveStreaming(
                thumbnailURL = "https://upload.wikimedia.org/wikipedia/commons/thumb/e/ea/Android_logo_2023_%28stacked%29.svg/242px-Android_logo_2023_%28stacked%29.svg.png",
                title = "안녕하세요. 방송 시작합니다.",
                content = "오늘만 이루어지는 하아리이트 방송 시작하겠습니다. 모두 모두 모여주세요."
            ),
            LiveStreaming(
                thumbnailURL = "https://upload.wikimedia.org/wikipedia/commons/thumb/e/ea/Android_logo_2023_%28stacked%29.svg/242px-Android_logo_2023_%28stacked%29.svg.png",
                title = "안녕하세요. 방송 시작합니다.",
                content = "오늘만 이루어지는 하아리이트 방송 시작하겠습니다. 모두 모두 모여주세요."
            )
        )
    )
}

@Composable
fun LiveListScreen(
    liveStreamingList: List<LiveStreaming>
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(10.dp)) {
            LazyColumn {
                items(liveStreamingList) { liveStreaming ->
                    LiveStreamingItem(
                        thumbnailUrl = liveStreaming.thumbnailURL,
                        title = liveStreaming.title,
                        content = liveStreaming.content
                    )
                }
            }
        }
    }
}

@Composable
fun LiveStreamingItem(
    thumbnailUrl: String,
    title: String,
    content: String
) {
    Column(modifier = Modifier.padding(bottom = 10.dp)) {
        AsyncImage(
            model = thumbnailUrl,
            contentDescription = title,
            modifier = Modifier.fillMaxWidth().aspectRatio(1f)
        )

        Text(
            text = title,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp
        )

        Text(
            text = content,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 5.dp)
        )
    }
}

@Preview
@Composable
fun PreviewLiveListScreen() {
    MyApplicationTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            LiveListScreen(
                liveStreamingList = listOf(
                    LiveStreaming(
                        thumbnailURL = "https://upload.wikimedia.org/wikipedia/commons/thumb/e/ea/Android_logo_2023_%28stacked%29.svg/242px-Android_logo_2023_%28stacked%29.svg.png",
                        title = "안녕하세요. 방송 시작합니다.",
                        content = "오늘만 이루어지는 하아리이트 방송 시작하겠습니다. 모두 모두 모여주세요."
                    ),
                    LiveStreaming(
                        thumbnailURL = "https://upload.wikimedia.org/wikipedia/commons/thumb/e/ea/Android_logo_2023_%28stacked%29.svg/242px-Android_logo_2023_%28stacked%29.svg.png",
                        title = "안녕하세요. 방송 시작합니다.",
                        content = "오늘만 이루어지는 하아리이트 방송 시작하겠습니다. 모두 모두 모여주세요."
                    ),
                    LiveStreaming(
                        thumbnailURL = "https://upload.wikimedia.org/wikipedia/commons/thumb/e/ea/Android_logo_2023_%28stacked%29.svg/242px-Android_logo_2023_%28stacked%29.svg.png",
                        title = "안녕하세요. 방송 시작합니다.",
                        content = "오늘만 이루어지는 하아리이트 방송 시작하겠습니다. 모두 모두 모여주세요."
                    )
                )
            )
        }
    }
}