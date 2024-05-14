package com.pass.presentation.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import javax.annotation.concurrent.Immutable
import javax.inject.Inject

@HiltViewModel
class MyViewModel @Inject constructor(

): ViewModel(), ContainerHost<LoginState, LoginSideEffect> {

    override val container: Container<LoginState, LoginSideEffect> = container(
        initialState = LoginState()
    )

    fun onClickLogin() {

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

}