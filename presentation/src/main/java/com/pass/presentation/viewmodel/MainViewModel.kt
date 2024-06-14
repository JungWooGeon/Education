package com.pass.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.pass.domain.model.Video
import com.pass.presentation.intent.MainIntent
import com.pass.presentation.sideeffect.MainScreenSideEffect
import com.pass.presentation.state.screen.MainScreenState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel(), ContainerHost<MainScreenState, MainScreenSideEffect> {

    override val container: Container<MainScreenState, MainScreenSideEffect> = container (
        initialState = MainScreenState()
    )

    fun processIntent(intent: MainIntent) {
        when(intent) {
            is MainIntent.OnClickBackButton -> onClickBackButton()
            is MainIntent.CloseVideoPlayer -> closeVideoPlayer()
            is MainIntent.ShowVideoPlayer -> showVideoPlayer(video = intent.video)
            is MainIntent.NavigateScreenRoute -> navigateScreenRoute(intent.screenRoute)
        }
    }

    private fun onClickBackButton() = intent {
        if(System.currentTimeMillis() - state.backPressedTimeState <= 1000L) {
            postSideEffect(MainScreenSideEffect.FinishActivity)
        } else {
            reduce {
                state.copy(backPressedTimeState = System.currentTimeMillis())
            }
            postSideEffect(MainScreenSideEffect.Toast("한 번 더 누르면 종료합니다."))
        }
    }

    private fun closeVideoPlayer() = intent {
        reduce {
            state.copy(showPlayerState = null)
        }
    }

    private fun showVideoPlayer(video: Video) = intent {
        if (state.showPlayerState != null) {
            reduce {
                state.copy(showPlayerState = null)
            }
            delay(100)
        }

        reduce {
            state.copy(showPlayerState = video)
        }
    }

    private fun navigateScreenRoute(screenRoute: String) = intent {
        postSideEffect(MainScreenSideEffect.NavigateScreenRoute(screenRoute))
    }
}