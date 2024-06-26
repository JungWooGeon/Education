package com.pass.presentation.view.screen

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pass.presentation.intent.SignInIntent
import com.pass.presentation.sideeffect.SignInSideEffect
import com.pass.presentation.view.component.CodeBridgeTextField
import com.pass.presentation.viewmodel.SignInViewModel
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun SignInScreen(
    viewModel: SignInViewModel = hiltViewModel(),
    onNavigateToSignUpScreen: () -> Unit,
    onNavigateToProfileScreen: () -> Unit
) {
    val context = LocalContext.current

    // TextField 한글 자소 분리 현상 완화를 위해 UI 상태로 적용
    var id by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    viewModel.collectSideEffect { sideEffect ->
        when(sideEffect) {
            is SignInSideEffect.Toast -> Toast.makeText(context, sideEffect.message, Toast.LENGTH_SHORT).show()
            is SignInSideEffect.NavigateToProfileScreen -> {
                Toast.makeText(context, "로그인에 성공하였습니다.", Toast.LENGTH_SHORT).show()
                onNavigateToProfileScreen()
            }
        }
    }

    SignInScreen(
        id = id,
        password = password,
        onChangeId = { id = it },
        onChangePassword = { password = it },
        onClickSignIn = { viewModel.processIntent(SignInIntent.OnClickSignIn(id, password)) },
        onNavigateToSignUpScreen = onNavigateToSignUpScreen
    )
}

@Composable
fun SignInScreen(
    id: String,
    password: String,
    onChangeId: (String) -> Unit,
    onChangePassword: (String) -> Unit,
    onClickSignIn: () -> Unit,
    onNavigateToSignUpScreen: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 40.dp)
    ) {

        CodeBridgeTextField(
            modifier = Modifier,
            value = id,
            onChangeValue = onChangeId,
            placeHolderValue = "id"
        )

        CodeBridgeTextField(
            modifier = Modifier.padding(top = 20.dp),
            value = password,
            onChangeValue = onChangePassword,
            placeHolderValue = "password",
            visualTransformation = PasswordVisualTransformation()
        )

        Button(
            modifier = Modifier.padding(top = 20.dp),
            contentPadding = PaddingValues(16.dp),
            onClick = onClickSignIn
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "Sign In",
                textAlign = TextAlign.Center,
                fontSize = 14.sp
            )
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.weight(1f))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(text = "Don't have an account?", fontSize = 12.sp)
            Text(
                text = "Sign Up",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 12.sp,
                modifier = Modifier.clickable { onNavigateToSignUpScreen() }
            )
        }
    }
}