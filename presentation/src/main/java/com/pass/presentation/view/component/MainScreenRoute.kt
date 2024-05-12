package com.pass.presentation.view.component

import com.pass.presentation.R

sealed class MainScreenRoute(
    val title: Int,
    val icon: Int,
    val screenRoute: String
) {
    companion object {
        const val LIVE_SCREEN_ROUTE = "LiveScreenRoute"
        const val VIDEO_SCREEN_ROUTE = "VideoScreenRoute"
    }

    data object LiveListScreen : MainScreenRoute(
        title = R.string.live_screen,
        icon = R.drawable.ic_live,
        screenRoute = LIVE_SCREEN_ROUTE
    )

    data object VideoListScreen : MainScreenRoute(
        title = R.string.video_screen,
        icon = R.drawable.ic_video,
        screenRoute = VIDEO_SCREEN_ROUTE
    )
}