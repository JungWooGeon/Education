package com.pass.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.pass.domain.usecase.GetUserProfileUseCase
import com.pass.domain.usecase.SignOutUseCase
import com.pass.domain.usecase.UpdateUserProfileNameUseCase
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
class ProfileViewModel @Inject constructor(
    private val signOutUseCase: SignOutUseCase,
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val updateUserProfileNameUseCase: UpdateUserProfileNameUseCase
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
                        userName = profile.name,
                        editDialogUserName = profile.name
                    )
                }
            }.onFailure { exception ->
                // 프로필 조회 실패 시 토스트 메시지 출력 후 로그아웃
                postSideEffect(
                    ProfileSideEffect.Toast(
                        message = exception.message ?: "프로필 조회에 실패하였습니다. 다시 로그인해주세요."
                    )
                )
                onClickSignOut()
            }
        }
    }

    fun onClickSignOut() = intent {
        signOutUseCase()
        postSideEffect(ProfileSideEffect.NavigateSignInScreen)
    }

    fun onClickEditButton() = intent {
        reduce {
            state.copy(onEditDialog = true)
        }
    }

    fun onCancelEditPopUp() = intent {
        reduce {
            state.copy(
                onEditDialog = false,
                editDialogUserName = state.userName
            )
        }
    }

    fun onChangeEditDialogUserName(editDialogUserName: String) = intent {
        reduce {
            state.copy(
                editDialogUserName = editDialogUserName
            )
        }
    }

    fun onClickSaveEditDialogButton() = intent {
        updateUserProfileNameUseCase(name = state.editDialogUserName).collect { result ->
            result.onSuccess {
                reduce {
                    state.copy(
                        userName = state.editDialogUserName,
                        onEditDialog = false
                    )
                }
                postSideEffect(ProfileSideEffect.Toast("닉네임을 변경하였습니다."))
            }.onFailure {
                reduce {
                    state.copy(onEditDialog = false)
                }
                postSideEffect(ProfileSideEffect.Toast("닉네임 변경에 실패하였습니다. 잠시 후 다시 시도해주세요."))
            }
        }
    }
}

@Immutable
data class ProfileState(
    val userProfileURL: String = "",
    val userName: String = "",
    val onEditDialog: Boolean = false,
    val editDialogUserName: String = ""
)

sealed interface ProfileSideEffect {
    data class Toast(val message: String) : ProfileSideEffect
    data object NavigateSignInScreen : ProfileSideEffect
}