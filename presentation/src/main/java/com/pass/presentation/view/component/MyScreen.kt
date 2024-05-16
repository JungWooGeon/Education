package com.pass.presentation.view.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pass.presentation.state.MyScreenRoute
import com.pass.presentation.viewmodel.MyViewModel

@Composable
fun MyScreen(viewModel: MyViewModel = hiltViewModel()) {
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

            composable(MyScreenRoute.SignUpScreen.screenRoute) {
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

            composable(MyScreenRoute.ProfileScreen.screenRoute) {
                ProfileScreen(
                    onNavigateToSignInScreen = {
                        myScreenNavController.navigate(MyScreenRoute.SignInScreen.screenRoute) {
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