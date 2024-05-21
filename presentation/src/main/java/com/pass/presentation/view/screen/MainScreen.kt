package com.pass.presentation.view.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.pass.presentation.R
import com.pass.presentation.state.MainScreenRoute

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(logoResource: Int) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier.padding(15.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = logoResource),
                    contentDescription = null,
                    modifier = Modifier
                        .size(40.dp)
                        .padding(end = 10.dp)
                )

                Text(
                    text = stringResource(id = R.string.app_name),
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        bottomBar = {
            BottomNavigationBar(
                navController = navController,
                currentRoute = currentRoute
            )
        }
    ) {
        Box(Modifier.padding(it)) {
            NavigationGraph(navController)
        }
    }
}

@Composable
fun BottomNavigationBar(
    navController: NavHostController,
    currentRoute: String?
) {
    val bottomNavItems = listOf(
        MainScreenRoute.LiveListScreen,
        MainScreenRoute.VideoListScreen,
        MainScreenRoute.MyScreen
    )

    NavigationBar(
        containerColor = Color.White
    ) {
        bottomNavItems.forEach { navItem ->
            NavigationBarItem(
                selected = currentRoute == navItem.screenRoute,
                onClick = {
                    navController.navigate(navItem.screenRoute) {
                        navController.graph.startDestinationRoute?.let {
                            popUpTo(it) { saveState = true }
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                label = { Text(stringResource(id = navItem.title), fontSize = 9.sp) },
                icon = {
                    Icon(
                        painter = painterResource(id = navItem.icon),
                        contentDescription = stringResource(id = navItem.title),
                        tint = if (currentRoute == navItem.screenRoute) MaterialTheme.colorScheme.primary else Color.LightGray,
                        modifier = Modifier
                            .width(26.dp)
                            .height(26.dp)
                    )
                },
                alwaysShowLabel = false,
                colors = NavigationBarItemDefaults.colors(
                    selectedTextColor = Color.Black,
                    unselectedTextColor = Color.White,
                    indicatorColor = Color.White
                )
            )
        }
    }
}

@Composable
fun NavigationGraph(
    navController: NavHostController
) {
    NavHost(navController = navController, startDestination =  MainScreenRoute.LiveListScreen.screenRoute) {
        composable(MainScreenRoute.LiveListScreen.screenRoute) {
            LiveListScreen()
        }

        composable(MainScreenRoute.VideoListScreen.screenRoute) {
            VideoListScreen()
        }

        composable(MainScreenRoute.MyScreen.screenRoute) {
            MyScreen()
        }
    }
}