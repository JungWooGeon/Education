package com.pass.domain.model

data class LiveStreaming(
    val broadcastId: String,
    val thumbnailURL: String,
    val title: String,
    val userProfileURL: String,
    val userName: String
)