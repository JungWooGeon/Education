package com.pass.presentation.view.component

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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pass.presentation.ui.theme.MyApplicationTheme
import com.pass.presentation.viewmodel.MyViewModel
import org.orbitmvi.orbit.compose.collectAsState

@Composable
fun MyScreen(viewModel: MyViewModel = hiltViewModel()) {
    val loginState = viewModel.collectAsState().value
    MyScreen(
        isLogin = false,
        id = loginState.id,
        password = loginState.password,
        onChangeId = viewModel::onChangeId,
        onChangePassword = viewModel::onChangePassword,
        onClickLogin = viewModel::onClickLogin
    )
}

@Composable
fun MyScreen(
    isLogin: Boolean,
    id: String,
    password: String,
    onChangeId: (String) -> Unit,
    onChangePassword: (String) -> Unit,
    onClickLogin: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 40.dp)
    ) {

        LoginInputTextField(
            modifier = Modifier,
            value = id,
            onChangeValue = onChangeId,
            placeHolderValue = "id"
        )

        LoginInputTextField(
            modifier = Modifier.padding(top = 20.dp),
            value = password,
            onChangeValue = onChangePassword,
            placeHolderValue = "password",
            visualTransformation = PasswordVisualTransformation()
        )

        Button(
            modifier = Modifier.padding(top = 20.dp),
            contentPadding = PaddingValues(16.dp),
            onClick = onClickLogin
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "Login",
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
            Text(text = "Sign up", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
        }
    }
}

@Preview
@Composable
fun PreviewMyScreen() {
    MyApplicationTheme {
        Surface {
            MyScreen()
        }
    }
}