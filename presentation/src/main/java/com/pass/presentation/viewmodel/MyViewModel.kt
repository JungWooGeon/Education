package com.pass.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.pass.domain.usecase.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import javax.annotation.concurrent.Immutable
import javax.inject.Inject

@HiltViewModel
class MyViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase
): ViewModel(), ContainerHost<LoginState, LoginSideEffect> {

    override val container: Container<LoginState, LoginSideEffect> = container(
        initialState = LoginState()
    )

    fun onClickLogin() = intent {
        val id = state.id
        val password = state.password

        loginUseCase(id, password).collect { isLoginSuccess ->
            if (isLoginSuccess) {
                postSideEffect(LoginSideEffect.Toast(message = "로그인에 성공하였습니다."))
            } else {
                postSideEffect(LoginSideEffect.Toast(message = "로그인에 실패하였습니다."))
            }
        }
    }

    fun onChangeId(id: String) = intent {
        reduce {
            state.copy(id = id)
        }
    }

    fun onChangePassword(password: String) = intent {
        reduce {
            state.copy(password = password)
        }
    }
}

@Immutable
data class LoginState(
    val id: String = "",
    val password: String = ""
)

sealed interface LoginSideEffect {
    class Toast(val message: String): LoginSideEffect
}