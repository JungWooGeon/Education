package com.pass.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.pass.domain.usecase.SignUpUseCase
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
class SignUpViewModel @Inject constructor(
    private val signUpUseCase: SignUpUseCase
) : ViewModel(), ContainerHost<SignUpState, SignUpSideEffect> {

    override val container: Container<SignUpState, SignUpSideEffect> = container(
        initialState = SignUpState()
    )

    fun onClickSignUp() = intent {
        val id = state.id
        val password = state.password
        val verifyPassword = state.verifyPassword

        signUpUseCase(id, password, verifyPassword).collect { result ->
            result.onSuccess {
                postSideEffect(SignUpSideEffect.NavigateToProfileScreen)
            }.onFailure { exception ->
                postSideEffect(SignUpSideEffect.Toast(message = exception.message ?: "회원가입 실패 : Unknown error"))
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

    fun onChangeVerifyPassword(verifyPassword: String) = intent {
        reduce {
            state.copy(verifyPassword = verifyPassword)
        }
    }
}

@Immutable
data class SignUpState(
    val id: String = "",
    val password: String = "",
    val verifyPassword: String = ""
)

sealed interface SignUpSideEffect {
    data object NavigateToProfileScreen : SignUpSideEffect
    data class Toast(val message: String) : SignUpSideEffect
}