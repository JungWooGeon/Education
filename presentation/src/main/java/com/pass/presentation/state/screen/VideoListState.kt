package com.pass.presentation.state.screen

import com.pass.domain.model.Video
import javax.annotation.concurrent.Immutable

@Immutable
data class VideoListState(
    val videoList: List<Video> = emptyList(),
    val isLoggedIn: Boolean = false
)