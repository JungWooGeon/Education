package com.pass.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.pass.domain.usecase.SignUpUseCase
import com.pass.presentation.intent.SignUpIntent
import com.pass.presentation.sideeffect.SignUpSideEffect
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val signUpUseCase: SignUpUseCase
) : ViewModel(), ContainerHost<Unit, SignUpSideEffect> {

    override val container: Container<Unit, SignUpSideEffect> = container(initialState = Unit)

    fun processIntent(intent: SignUpIntent) {
        when(intent) {
            is SignUpIntent.OnClickSignUp -> onClickSignUp(intent.id, intent.password, intent.verifyPassword)
        }
    }

    private fun onClickSignUp(id: String, password: String, verifyPassword: String) = intent {
        val result = signUpUseCase(id, password, verifyPassword).first()
        result.onSuccess {
            postSideEffect(SignUpSideEffect.NavigateToProfileScreen)
        }.onFailure { exception ->
            postSideEffect(SignUpSideEffect.Toast(message = exception.message ?: "회원가입 실패 : Unknown error"))
        }
    }
}