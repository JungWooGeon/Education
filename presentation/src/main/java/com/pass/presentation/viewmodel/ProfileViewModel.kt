package com.pass.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pass.domain.usecase.GetUserProfileUseCase
import com.pass.domain.usecase.SignOutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import javax.annotation.concurrent.Immutable
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val signOutUseCase: SignOutUseCase,
    private val getUserProfileUseCase: GetUserProfileUseCase
) : ViewModel(), ContainerHost<ProfileState, ProfileSideEffect> {

    override val container: Container<ProfileState, ProfileSideEffect> = container(
        initialState = ProfileState()
    )

    init {
        readProfile()
    }

    private fun readProfile() = intent {
        getUserProfileUseCase().collect { result ->
            result.onSuccess { profile ->
                reduce {
                    state.copy(
                        userProfileURL = profile.pictureUrl,
                        userName = profile.name
                    )
                }
            }.onFailure { exception ->
                // 프로필 조회 실패 시 토스트 메시지 출력 후 로그아웃
                postSideEffect(ProfileSideEffect.Toast(message = exception.message ?: "프로필 조회에 실패하였습니다. 다시 로그인해주세요."))
                onClickSignOut()
            }
        }
    }

    fun onClickSignOut() = intent {
        signOutUseCase()
        postSideEffect(ProfileSideEffect.NavigateSignInScreen)
    }
}

@Immutable
data class ProfileState(
    val userProfileURL: String = "",
    val userName : String = ""
)

sealed interface ProfileSideEffect {
    data class Toast(val message: String) : ProfileSideEffect
    data object NavigateSignInScreen : ProfileSideEffect
}