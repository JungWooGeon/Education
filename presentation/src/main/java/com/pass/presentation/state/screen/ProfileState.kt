package com.pass.presentation.state.screen

import com.pass.domain.model.Video
import javax.annotation.concurrent.Immutable

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