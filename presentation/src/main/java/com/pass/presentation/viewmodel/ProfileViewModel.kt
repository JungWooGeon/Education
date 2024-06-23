package com.pass.presentation.viewmodel

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.pass.domain.usecase.DeleteVideoUseCase
import com.pass.domain.usecase.GetUserProfileUseCase
import com.pass.domain.usecase.SignOutUseCase
import com.pass.domain.usecase.UpdateUserProfileNameUseCase
import com.pass.domain.usecase.UpdateUserProfilePictureUseCase
import com.pass.domain.util.URLCodec
import com.pass.presentation.intent.ProfileIntent
import com.pass.presentation.sideeffect.ProfileSideEffect
import com.pass.presentation.state.screen.ProfileState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val signOutUseCase: SignOutUseCase,
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val updateUserProfileNameUseCase: UpdateUserProfileNameUseCase,
    private val updateUserProfilePicture: UpdateUserProfilePictureUseCase,
    private val deleteVideoUseCase: DeleteVideoUseCase<Bitmap>,
    private val urlCodec: URLCodec<Uri>
) : ViewModel(), ContainerHost<ProfileState, ProfileSideEffect> {

    override val container: Container<ProfileState, ProfileSideEffect> = container(
        initialState = ProfileState()
    )

    fun processIntent(intent: ProfileIntent) {
        when(intent) {
            is ProfileIntent.ReadProfile -> readProfile()
            is ProfileIntent.OnClickSignOut -> onClickSignOut()
            is ProfileIntent.OnClickEditButton -> onClickEditButton()
            is ProfileIntent.OnCancelEditPopUp -> onCancelEditPopUp()
            is ProfileIntent.OnClickSaveEditDialogButton -> onClickSaveEditDialogButton(intent.editDialogUserName)
            is ProfileIntent.OnChangeUserProfilePicture -> onChangeUserProfilePicture(intent.uri)
            is ProfileIntent.OpenDeleteModalBottomSheet -> openDeleteModalBottomSheet(intent.videoIdx)
            is ProfileIntent.CloseDeleteModalBottomSheet -> closeDeleteModalBottomSheet()
            is ProfileIntent.DeleteVideoItem -> deleteVideoItem()
            is ProfileIntent.OnNavigateToAddVideoScreen -> navigateToAddVideoScreen(intent.uri)
        }
    }

    private fun readProfile() = intent {
        val result = getUserProfileUseCase().first()
        result.onSuccess { profile ->
            reduce {
                state.copy(
                    userProfileURL = urlCodec.urlDecode(profile.pictureUrl),
                    userName = profile.name,
                    videoList = profile.videoList
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

    private fun onClickSignOut() = intent {
        signOutUseCase()
        postSideEffect(ProfileSideEffect.NavigateSignInScreen)
    }

    private fun onClickEditButton() = intent {
        reduce {
            state.copy(onEditDialog = true)
        }
    }

    private fun onCancelEditPopUp() = intent {
        reduce {
            state.copy(
                onEditDialog = false
            )
        }
    }

    private fun onClickSaveEditDialogButton(editDialogUserName: String) = intent {
        val result = updateUserProfileNameUseCase(name = editDialogUserName).first()
        result.onSuccess {
            reduce {
                state.copy(
                    userName = editDialogUserName,
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

    private fun onChangeUserProfilePicture(uri: Uri?) = intent {
        if (uri == null) {
            postSideEffect(ProfileSideEffect.Toast("프로필 사진 변경을 취소하였습니다."))
        } else {
            val result = updateUserProfilePicture(withContext(Dispatchers.IO) {
                urlCodec.urlEncode(uri)
            }).first()
            result.onSuccess { pictureUrl ->
                reduce {
                    state.copy(userProfileURL = urlCodec.urlDecode(pictureUrl))
                }
                postSideEffect(ProfileSideEffect.Toast("프로필 사진을 변경하였습니다."))
            }.onFailure {
                postSideEffect(ProfileSideEffect.Toast("프로필 사진 변경에 실패하였습니다."))
            }
        }
    }

    private fun openDeleteModalBottomSheet(videoIdx: Int) = intent {
        reduce {
            state.copy(
                deleteVideoIdx = videoIdx,
                isOpenDeleteModalBottomSheet = true
            )
        }
    }

    private fun closeDeleteModalBottomSheet() = intent {
        reduce {
            state.copy(
                isOpenDeleteModalBottomSheet = false
            )
        }
    }

    private fun deleteVideoItem() = intent {
        reduce {
            state.copy(
                isOpenDeleteModalBottomSheet = false
            )
        }

        if (state.deleteVideoIdx != null) {
            val result = deleteVideoUseCase(state.videoList[state.deleteVideoIdx!!]).first()
            result.onSuccess {
                reduce {
                    state.copy(
                        videoList = state.videoList.toMutableList().apply {
                            state.deleteVideoIdx?.let { it1 -> this.removeAt(it1) }
                        }.toList()
                    )
                }
                postSideEffect(ProfileSideEffect.Toast("동영상을 삭제하였습니다."))
            }.onFailure { e ->
                postSideEffect(ProfileSideEffect.Toast(e.message ?: "동영상 삭제에 실패하였습니다."))
            }
        }
    }

    private fun navigateToAddVideoScreen(uri: Uri) = intent {
        val uriString = urlCodec.urlEncode(uri)
        postSideEffect(ProfileSideEffect.NavigateToAddVideoScreen(uriString))
    }
}