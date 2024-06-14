package com.pass.presentation.state.screen

import com.pass.domain.model.Video
import javax.annotation.concurrent.Immutable

@Immutable
data class MainScreenState(
    val backPressedTimeState: Long = 0L,
    val showPlayerState: Video? = null
)