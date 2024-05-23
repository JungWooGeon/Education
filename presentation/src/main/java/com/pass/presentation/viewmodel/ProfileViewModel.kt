package com.pass.presentation.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.pass.domain.model.Video
import com.pass.domain.usecase.DeleteVideoUseCase
import com.pass.domain.usecase.GetUserProfileUseCase
import com.pass.domain.usecase.SignOutUseCase
import com.pass.domain.usecase.UpdateUserProfileNameUseCase
import com.pass.domain.usecase.UpdateUserProfilePictureUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import javax.annotation.concurrent.Immutable
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val signOutUseCase: SignOutUseCase,
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val updateUserProfileNameUseCase: UpdateUserProfileNameUseCase,
    private val updateUserProfilePicture: UpdateUserProfilePictureUseCase,
    private val deleteVideoUseCase: DeleteVideoUseCase
) : ViewModel(), ContainerHost<ProfileState, ProfileSideEffect> {

    override val container: Container<ProfileState, ProfileSideEffect> = container(
        initialState = ProfileState()
    )

    fun readProfile() = intent {
        getUserProfileUseCase().collect { result ->
            result.onSuccess { profile ->
                reduce {
                    state.copy(
                        userProfileURL = URLDecoder.decode(profile.pictureUrl, StandardCharsets.UTF_8.toString()),
                        userName = profile.name,
                        editDialogUserName = profile.name,
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

    fun onChangeUserProfilePicture(uri: Uri?) = intent {
        if (uri == null) {
            postSideEffect(ProfileSideEffect.Toast("프로필 사진 변경을 취소하였습니다."))
        } else {
            updateUserProfilePicture(withContext(Dispatchers.IO) {
                URLEncoder.encode(uri.toString(), StandardCharsets.UTF_8.toString())
            }).collect { result ->
                result.onSuccess { pictureUrl ->
                    reduce {
                        state.copy(userProfileURL = URLDecoder.decode(pictureUrl, StandardCharsets.UTF_8.toString()))
                    }
                    postSideEffect(ProfileSideEffect.Toast("프로필 사진을 변경하였습니다."))
                }.onFailure {
                    postSideEffect(ProfileSideEffect.Toast("프로필 사진 변경에 실패하였습니다."))
                }
            }
        }
    }

    fun openDeleteModalBottomSheet(videoIdx: Int) = intent {
        reduce {
            state.copy(
                deleteVideoIdx = videoIdx,
                isOpenDeleteModalBottomSheet = true
            )
        }
    }

    fun closeDeleteModalBottomSheet() = intent {
        reduce {
            state.copy(
                isOpenDeleteModalBottomSheet = false
            )
        }
    }

    fun deleteVideoItem() = intent {
        reduce {
            state.copy(
                isOpenDeleteModalBottomSheet = false
            )
        }

        if (state.deleteVideoIdx != null) {
            deleteVideoUseCase(state.videoList[state.deleteVideoIdx!!]).collect { result ->
                result.onSuccess {
                    reduce {
                        state.copy(
                            videoList = state.videoList.toMutableList().apply {
                                state.deleteVideoIdx?.let { it1 -> this.removeAt(it1) }
                            }.toList()
                        )
                    }
                    postSideEffect(ProfileSideEffect.Toast("동영상 삭제에 성공하였습니다."))
                }.onFailure { e ->
                    postSideEffect(ProfileSideEffect.Toast(e.message ?: "동영상 삭제에 실패하였습니다."))
                }
            }
        }
    }
}

@Immutable
data class ProfileState(
    val userProfileURL: String = "",
    val userName: String = "",
    val videoList: List<Video> = listOf(),

    val onEditDialog: Boolean = false,
    val editDialogUserName: String = "",

    val isOpenDeleteModalBottomSheet: Boolean = false,
    val deleteVideoIdx: Int? = null
)

sealed interface ProfileSideEffect {
    data class Toast(val message: String) : ProfileSideEffect
    data object NavigateSignInScreen : ProfileSideEffect
}