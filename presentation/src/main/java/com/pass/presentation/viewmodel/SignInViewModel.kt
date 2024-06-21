package com.pass.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.pass.domain.usecase.SignInUseCase
import com.pass.presentation.intent.SignInIntent
import com.pass.presentation.sideeffect.SignInSideEffect
import dagger.hilt.android.lifecycle.HiltViewModel
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val signInUseCase: SignInUseCase
) : ViewModel(), ContainerHost<Unit, SignInSideEffect> {

    override val container: Container<Unit, SignInSideEffect> = container(Unit)

    fun processIntent(intent: SignInIntent) {
        when(intent) {
            is SignInIntent.OnClickSignIn -> onClickSignIn(intent.id, intent.password)
        }
    }

    private fun onClickSignIn(id: String, password: String) = intent {
        signInUseCase(id, password).collect { result ->
            result.onSuccess {
                postSideEffect(SignInSideEffect.NavigateToProfileScreen)
            }.onFailure { exception ->
                postSideEffect(SignInSideEffect.Toast(message = exception.message ?: "로그인 실패 : Unknown error"))
            }
        }
    }
}