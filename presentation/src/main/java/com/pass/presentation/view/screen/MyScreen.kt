package com.pass.presentation.view.screen

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.pass.domain.model.Video
import com.pass.presentation.intent.MyIntent
import com.pass.presentation.sideeffect.MyScreenSideEffect
import com.pass.presentation.state.route.MyScreenRoute
import com.pass.presentation.state.screen.MyScreenState
import com.pass.presentation.view.component.AnimatedScreen
import com.pass.presentation.viewmodel.MyViewModel
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun MyScreen(
    viewModel: MyViewModel = hiltViewModel(),
    showVideoStreamingPlayer: (Video) -> Unit,
    onCloseVideoPlayer: () -> Unit
) {
    val myScreenState = viewModel.collectAsState().value
    val myScreenNavController = rememberNavController()

    viewModel.collectSideEffect { sideEffect ->
        when(sideEffect) {
            is MyScreenSideEffect.NavigateScreenRoute -> {
                myScreenNavController.navigate(sideEffect.screenRoute) {
                    myScreenNavController.graph.startDestinationRoute?.let {
                        popUpTo(it) { saveState = true }
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        }
    }

    MyScreen(
        myScreenState = myScreenState,
        myScreenNavController = myScreenNavController,
        showVideoStreamingPlayer = showVideoStreamingPlayer,
        onCloseVideoPlayer = onCloseVideoPlayer,
        onNavigateScreenRoute = { viewModel.processIntent(MyIntent.NavigateScreenRoute(it)) }
    )
}

@Composable
fun MyScreen(
    myScreenState: MyScreenState,
    myScreenNavController: NavHostController,
    showVideoStreamingPlayer: (Video) -> Unit,
    onCloseVideoPlayer: () -> Unit,
    onNavigateScreenRoute: (screenRoute: String) -> Unit
) {
    myScreenState.isSignedInState?.let { isSignedIn ->
        val startScreen = if (isSignedIn) {
            MyScreenRoute.ProfileScreen.screenRoute
        } else {
            MyScreenRoute.SignInScreen.screenRoute
        }

        NavHost(navController = myScreenNavController, startDestination = startScreen) {
            composable(MyScreenRoute.SignInScreen.screenRoute) {
                AnimatedScreen {
                    SignInScreen(
                        onNavigateToSignUpScreen = { onNavigateScreenRoute(MyScreenRoute.SignUpScreen.screenRoute) },
                        onNavigateToProfileScreen = { onNavigateScreenRoute(MyScreenRoute.ProfileScreen.screenRoute) }
                    )
                }
            }

            composable(MyScreenRoute.SignUpScreen.screenRoute) {
                AnimatedScreen {
                    SignUpScreen(
                        onNavigateToSignInScreen = { onNavigateScreenRoute(MyScreenRoute.SignInScreen.screenRoute) },
                        onNavigateToProfileScreen = { onNavigateScreenRoute(MyScreenRoute.ProfileScreen.screenRoute) }
                    )
                }
            }

            composable(MyScreenRoute.ProfileScreen.screenRoute) {
                AnimatedScreen {
                    ProfileScreen(
                        onNavigateToSignInScreen = { onNavigateScreenRoute(MyScreenRoute.SignInScreen.screenRoute) },
                        onNavigateToAddVideoScreen = { videoUri ->
                            onCloseVideoPlayer()
                            onNavigateScreenRoute(MyScreenRoute.AddVideoScreen.createSelectedVideoUri(videoUri))
                        },
                        onClickVideoItem = { video ->
                            showVideoStreamingPlayer(video)
                        }
                    )
                }
            }

            composable(
                route = MyScreenRoute.AddVideoScreen.screenRoute,
                arguments = listOf(navArgument("videoUri") { type = NavType.StringType })
            ) { backStackEntry ->
                val videoUri = backStackEntry.arguments?.getString("videoUri")
                videoUri?.let {
                    AnimatedScreen {
                        AddVideoScreen(
                            videoUri = videoUri,
                            onNavigateProfileScreen = {
                                myScreenNavController.popBackStack()
                            }
                        )
                    }
                }
            }
        }
    }
}