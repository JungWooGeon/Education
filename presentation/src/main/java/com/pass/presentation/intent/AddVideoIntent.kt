package com.pass.presentation.intent

sealed class AddVideoIntent {
    data class CreateVideoThumbnail(val videoUri: String) : AddVideoIntent()
    data class OnChangeTitle(val title: String) : AddVideoIntent()
    data object OnClickUploadButton : AddVideoIntent()
}