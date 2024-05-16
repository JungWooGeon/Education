package com.pass.presentation.view.component

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pass.presentation.state.MyScreenRoute
import com.pass.presentation.viewmodel.LoginSideEffect
import com.pass.presentation.viewmodel.MyViewModel
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun MyScreen(viewModel: MyViewModel = hiltViewModel()) {
    val loginState = viewModel.collectAsState().value
    val context = LocalContext.current

    viewModel.collectSideEffect { sideEffect ->
        when(sideEffect) {
            is LoginSideEffect.Toast -> Toast.makeText(context, sideEffect.message, Toast.LENGTH_SHORT).show()
        }
    }

    val myScreenNavController = rememberNavController()
    NavHost(navController = myScreenNavController, startDestination = MyScreenRoute.LoginScreen.screenRoute) {
        composable(MyScreenRoute.LoginScreen.screenRoute) {
            LoginScreen(
                isLogin = false,
                id = loginState.id,
                password = loginState.password,
                onChangeId = viewModel::onChangeId,
                onChangePassword = viewModel::onChangePassword,
                onClickLogin = viewModel::onClickLogin
            )
        }

        composable(MyScreenRoute.ProfileScreen.screenRoute) {

        }
    }
}