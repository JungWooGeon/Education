package com.pass.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.pass.domain.usecase.SignInUseCase
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
class SignInViewModel @Inject constructor(
    private val signInUseCase: SignInUseCase
) : ViewModel(), ContainerHost<SignInState, SignInSideEffect> {

    override val container: Container<SignInState, SignInSideEffect> = container(
        initialState = SignInState()
    )

    fun onClickSignIn() = intent {
        val id = state.id
        val password = state.password

        signInUseCase(id, password).collect { result ->
            result.onSuccess {
                postSideEffect(SignInSideEffect.NavigateToProfileScreen)
            }.onFailure { exception ->
                postSideEffect(SignInSideEffect.Toast(message = exception.message ?: "로그인 실패 : Unknown error"))
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
data class SignInState(
    val id: String = "",
    val password: String = ""
)

sealed interface SignInSideEffect {
    data object NavigateToProfileScreen : SignInSideEffect
    data class Toast(val message: String) : SignInSideEffect
}