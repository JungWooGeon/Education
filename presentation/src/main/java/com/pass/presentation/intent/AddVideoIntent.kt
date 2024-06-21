package com.pass.presentation.intent

sealed class AddVideoIntent {
    data class CreateVideoThumbnail(val videoUri: String) : AddVideoIntent()
    data class OnClickUploadButton(val title: String) : AddVideoIntent()
}