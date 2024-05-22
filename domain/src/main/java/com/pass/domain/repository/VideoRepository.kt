package com.pass.domain.repository

interface VideoRepository {
    fun createVideoThumbnail(videoUri: String): Result<String>
}