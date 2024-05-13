package com.pass.domain.model

data class LiveStreaming(
    val thumbnailURL: String,
    val title: String,
    val userProfileURL: String,
    val userName: String,
    val tag: List<String>
)