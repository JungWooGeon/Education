package com.pass.presentation.state

import com.pass.presentation.R

sealed class MainScreenRoute(
    val title: Int,
    val icon: Int,
    val screenRoute: String
) {
    companion object {
        const val LIVE_SCREEN_ROUTE = "LiveScreenRoute"
        const val VIDEO_SCREEN_ROUTE = "VideoScreenRoute"
        const val MY_SCREEN_ROUTE = "MyScreenRoute"
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

    data object MyScreen : MainScreenRoute(
        title = R.string.my_screen,
        icon = R.drawable.ic_my,
        screenRoute = MY_SCREEN_ROUTE
    )
}