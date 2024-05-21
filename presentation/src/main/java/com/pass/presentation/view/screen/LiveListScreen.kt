package com.pass.presentation.view.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.pass.domain.model.LiveStreaming
import com.pass.presentation.viewmodel.LiveListViewModel

@Composable
fun LiveListScreen(viewModel: LiveListViewModel = hiltViewModel()) {
    LiveListScreen(
        liveStreamingList = listOf(
            LiveStreaming(
                thumbnailURL = "https://upload.wikimedia.org/wikipedia/commons/thumb/e/ea/Android_logo_2023_%28stacked%29.svg/242px-Android_logo_2023_%28stacked%29.svg.png",
                title = "백엔드 자바에서 코틀린으로 바꾼 이유",
                userProfileURL = "https://www.shutterstock.com/shutterstock/photos/2351867989/display_1500/stock-vector-social-media-live-broadcast-icon-streaming-video-online-meeting-2351867989.jpg",
                userName = "ReileyT^T",
                tag = listOf("Java", "백엔드", "Kotlin")
            ),
            LiveStreaming(
                thumbnailURL = "https://upload.wikimedia.org/wikipedia/commons/thumb/e/ea/Android_logo_2023_%28stacked%29.svg/242px-Android_logo_2023_%28stacked%29.svg.png",
                title = "안녕하세요. React 첫 방송 시작합니다.",
                userProfileURL = "https://www.shutterstock.com/shutterstock/photos/2351867989/display_1500/stock-vector-social-media-live-broadcast-icon-streaming-video-online-meeting-2351867989.jpg",
                userName = "형독",
                tag = listOf("프론트엔드", "React")
            ),
            LiveStreaming(
                thumbnailURL = "https://www.shutterstock.com/shutterstock/photos/1007080735/display_1500/stock-photo-switchboard-for-live-broadcast-production-equipment-in-outside-broadcasting-van-1007080735.jpg",
                title = "Compose로 시작하는 WebRTC 프로젝트",
                userProfileURL = "https://www.shutterstock.com/shutterstock/photos/2351867989/display_1500/stock-vector-social-media-live-broadcast-icon-streaming-video-online-meeting-2351867989.jpg",
                userName = "콘텐츠제작소 | CONSO",
                tag = listOf("모바일", "Android")
            )
        )
    )
}

@Composable
fun LiveListScreen(
    liveStreamingList: List<LiveStreaming>
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(vertical = 10.dp)) {
            LazyColumn {
                items(liveStreamingList) { liveStreaming ->
                    LiveStreamingItem(
                        thumbnailUrl = liveStreaming.thumbnailURL,
                        title = liveStreaming.title,
                        userProfileUrl = liveStreaming.userProfileURL,
                        userName = liveStreaming.userName,
                        tag = liveStreaming.tag
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
    userProfileUrl: String,
    userName: String,
    tag: List<String>
) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)) {
        AsyncImage(
            model = thumbnailUrl,
            contentDescription = title,
            modifier = Modifier.aspectRatio(2f),
            contentScale = ContentScale.FillWidth
        )

        Row(
            modifier = Modifier.padding(top = 10.dp, start = 10.dp, end = 10.dp)
        ) {
            AsyncImage(
                model = userProfileUrl,
                contentDescription = null,
                clipToBounds = true,
                modifier = Modifier.size(40.dp)
            )

            Column(
                modifier = Modifier.padding(start = 10.dp)
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )

                Text(
                    text = userName,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 5.dp)
                )
            }
        }
    }
}