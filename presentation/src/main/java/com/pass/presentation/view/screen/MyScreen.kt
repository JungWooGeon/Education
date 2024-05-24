package com.pass.presentation.view.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.pass.domain.model.Video
import com.pass.presentation.state.MyScreenRoute
import com.pass.presentation.viewmodel.MyViewModel

@Composable
fun MyScreen(
    viewModel: MyViewModel = hiltViewModel(),
    showVideoStreamingPlayer: (Video) -> Unit
) {
    var startScreen: String?
    val isSignedInState = viewModel.isSignedInState.collectAsState()
    val myScreenNavController = rememberNavController()

    isSignedInState.value?.let { isSignedIn ->
        startScreen = if (isSignedIn) {
            MyScreenRoute.ProfileScreen.screenRoute
        } else {
            MyScreenRoute.SignInScreen.screenRoute
        }

        NavHost(navController = myScreenNavController, startDestination = startScreen!!) {
            composable(MyScreenRoute.SignInScreen.screenRoute) {
                AnimatedScreen {
                    SignInScreen(
                        onNavigateToSignUpScreen = {
                            myScreenNavController.navigate(MyScreenRoute.SignUpScreen.screenRoute) {
                                myScreenNavController.graph.startDestinationRoute?.let {
                                    popUpTo(it) { saveState = true }
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onNavigateToProfileScreen = {
                            myScreenNavController.navigate(MyScreenRoute.ProfileScreen.screenRoute) {
                                myScreenNavController.graph.startDestinationRoute?.let {
                                    popUpTo(it) { saveState = true }
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }

            composable(MyScreenRoute.SignUpScreen.screenRoute) {
                AnimatedScreen {
                    SignUpScreen(
                        onNavigateToSignInScreen = {
                            myScreenNavController.navigate(MyScreenRoute.SignInScreen.screenRoute) {
                                myScreenNavController.graph.startDestinationRoute?.let {
                                    popUpTo(it) { saveState = true }
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onNavigateToProfileScreen = {
                            myScreenNavController.navigate(MyScreenRoute.ProfileScreen.screenRoute) {
                                myScreenNavController.graph.startDestinationRoute?.let {
                                    popUpTo(it) { saveState = true }
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }

            composable(MyScreenRoute.ProfileScreen.screenRoute) {
                AnimatedScreen {
                    ProfileScreen(
                        onNavigateToSignInScreen = {
                            myScreenNavController.navigate(MyScreenRoute.SignInScreen.screenRoute) {
                                myScreenNavController.graph.startDestinationRoute?.let {
                                    popUpTo(it) { saveState = true }
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onNavigateToAddVideoScreen = { videoUri ->
                            myScreenNavController.navigate(
                                MyScreenRoute.AddVideoScreen.createSelectedVideoUri(
                                    videoUri
                                )
                            ) {
                                myScreenNavController.graph.startDestinationRoute?.let {
                                    popUpTo(it) { saveState = true }
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onClickVideoItem = { video ->
                            showVideoStreamingPlayer(video)
                        }
                    )
                }
            }

            composable(
                route = MyScreenRoute.AddVideoScreen.screenRoute,
                arguments = listOf(
                    navArgument("videoUri") {
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry ->
                val videoUri = backStackEntry.arguments?.getString("videoUri")
                videoUri?.let {
                    AnimatedScreen {
                        AddVideoScreen(
                            videoUri = videoUri,
                            onNavigateProfileScreen = {
                                myScreenNavController.navigate(MyScreenRoute.ProfileScreen.screenRoute) {
                                    myScreenNavController.graph.startDestinationRoute?.let {
                                        popUpTo(it) { saveState = true }
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}