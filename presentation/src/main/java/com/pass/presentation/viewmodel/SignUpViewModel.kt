package com.pass.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.pass.domain.usecase.SignUpUseCase
import com.pass.presentation.intent.SignUpIntent
import com.pass.presentation.sideeffect.SignUpSideEffect
import com.pass.presentation.state.screen.SignUpState
import dagger.hilt.android.lifecycle.HiltViewModel
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val signUpUseCase: SignUpUseCase
) : ViewModel(), ContainerHost<SignUpState, SignUpSideEffect> {

    override val container: Container<SignUpState, SignUpSideEffect> = container(
        initialState = SignUpState()
    )

    fun processIntent(intent: SignUpIntent) {
        when(intent) {
            is SignUpIntent.OnClickSignUp -> onClickSignUp()
            is SignUpIntent.OnChangeId -> onChangeId(intent.id)
            is SignUpIntent.OnChangePassword -> onChangePassword(intent.password)
            is SignUpIntent.OnChangeVerifyPassword -> onChangeVerifyPassword(intent.verifyPassword)
        }
    }

    private fun onClickSignUp() = intent {
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

    private fun onChangeId(id: String) = intent {
        reduce {
            state.copy(id = id)
        }
    }

    private fun onChangePassword(password: String) = intent {
        reduce {
            state.copy(password = password)
        }
    }

    private fun onChangeVerifyPassword(verifyPassword: String) = intent {
        reduce {
            state.copy(verifyPassword = verifyPassword)
        }
    }
}