package com.pass.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.pass.domain.usecase.IsSignedInUseCase
import com.pass.presentation.intent.MyIntent
import com.pass.presentation.sideeffect.MyScreenSideEffect
import com.pass.presentation.state.screen.MyScreenState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class MyViewModel @Inject constructor(
    private val isSignedInUseCase: IsSignedInUseCase
) : ViewModel(), ContainerHost<MyScreenState, MyScreenSideEffect> {

    override val container: Container<MyScreenState, MyScreenSideEffect> = container(
        initialState = MyScreenState()
    )

    init {
        getIsSinged()
    }

    fun processIntent(intent: MyIntent) {
        when(intent) {
            is MyIntent.NavigateScreenRoute -> { navigateScreenRoute(intent.screenRoute) }
        }
    }

    private fun getIsSinged() = intent {
        val result = isSignedInUseCase().first()
        reduce {
            state.copy(isSignedInState = result)
        }
    }

    private fun navigateScreenRoute(screeRoute: String) = intent {
        postSideEffect(MyScreenSideEffect.NavigateScreenRoute(screeRoute))
    }
}