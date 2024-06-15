package com.pass.presentation.intent

import android.net.Uri

sealed class ProfileIntent {
    data object ReadProfile : ProfileIntent()
    data object OnClickSignOut : ProfileIntent()
    data object OnClickEditButton : ProfileIntent()
    data object OnCancelEditPopUp : ProfileIntent()
    data class OnChangeEditDialogUserName(val editDialogUserName: String) : ProfileIntent()
    data object OnClickSaveEditDialogButton : ProfileIntent()
    data class OnChangeUserProfilePicture(val uri: Uri?) : ProfileIntent()
    data class OpenDeleteModalBottomSheet(val videoIdx: Int) : ProfileIntent()
    data object CloseDeleteModalBottomSheet : ProfileIntent()
    data object DeleteVideoItem : ProfileIntent()
    data class OnNavigateToAddVideoScreen(val uri: Uri) : ProfileIntent()
}