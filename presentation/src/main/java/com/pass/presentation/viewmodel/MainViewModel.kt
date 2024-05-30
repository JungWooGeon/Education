package com.pass.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.pass.domain.model.Video
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import javax.annotation.concurrent.Immutable
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(

) : ViewModel(), ContainerHost<MainScreenState, MainScreenSideEffect> {

    override val container: Container<MainScreenState, MainScreenSideEffect> = container (
        initialState = MainScreenState()
    )

    fun onClickBackButton() = intent {
        if(System.currentTimeMillis() - state.backPressedTimeState <= 1000L) {
            postSideEffect(MainScreenSideEffect.FinishActivity)
        } else {
            reduce {
                state.copy(backPressedTimeState = System.currentTimeMillis())
            }
            postSideEffect(MainScreenSideEffect.Toast("한 번 더 누르면 종료합니다."))
        }
    }

    fun onCloseVideoPlayer() = intent {
        reduce {
            state.copy(showPlayerState = null)
        }
    }

    fun showVideoStreamingPlayer(video: Video) = intent {
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
}

@Immutable
data class MainScreenState(
    val backPressedTimeState: Long = 0L,
    val showPlayerState: Video? = null
)

sealed interface MainScreenSideEffect {
    data class Toast(val message: String) : MainScreenSideEffect
    data object FinishActivity : MainScreenSideEffect
}