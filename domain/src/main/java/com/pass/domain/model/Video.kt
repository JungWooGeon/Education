package com.pass.domain.model

data class Video(
    val videoId: String,
    val userId: String,
    val videoThumbnailUrl: String,
    val videoTitle: String,
    val time: String,
    val videoUrl: String
)