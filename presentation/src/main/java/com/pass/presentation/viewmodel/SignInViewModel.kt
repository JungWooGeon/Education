package com.pass.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.pass.domain.usecase.SignInUseCase
import com.pass.presentation.intent.SignInIntent
import com.pass.presentation.sideeffect.SignInSideEffect
import com.pass.presentation.state.screen.SignInState
import dagger.hilt.android.lifecycle.HiltViewModel
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val signInUseCase: SignInUseCase
) : ViewModel(), ContainerHost<SignInState, SignInSideEffect> {

    override val container: Container<SignInState, SignInSideEffect> = container(
        initialState = SignInState()
    )

    fun processIntent(intent: SignInIntent) {
        when(intent) {
            is SignInIntent.OnClickSignIn -> onClickSignIn()
            is SignInIntent.OnChangeId -> onChangeId(intent.id)
            is SignInIntent.OnChangePassword -> onChangePassword(intent.password)
        }
    }

    private fun onClickSignIn() = intent {
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
}