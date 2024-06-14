package com.pass.presentation.state.screen

import com.pass.domain.model.LiveStreaming
import javax.annotation.concurrent.Immutable

@Immutable
data class LiveListState(
    val isLoggedIn: Boolean = false,
    val liveStreamingList: List<LiveStreaming> = emptyList()
)