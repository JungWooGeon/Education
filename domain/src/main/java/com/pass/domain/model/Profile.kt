package com.pass.domain.model

data class Profile(
    val name: String = "",
    val pictureUrl: String = "",
    val videoList: List<Video>
)