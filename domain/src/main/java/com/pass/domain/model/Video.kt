package com.pass.domain.model

data class Video(
    val videoId: String,
    val userId: String,
    val videoThumbnailUrl: String,
    val videoTitle: String,
    val agoTime: String,
    val videoUrl: String
)