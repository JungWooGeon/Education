package com.pass.data.repository

import android.graphics.Bitmap
import com.google.firebase.auth.FirebaseAuth
import com.pass.data.service.database.VideoService
import com.pass.data.util.MediaUtil
import com.pass.domain.model.Video
import com.pass.domain.repository.VideoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class VideoRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val mediaUtil: MediaUtil,
    private val videoService: VideoService
) : VideoRepository<Bitmap> {

    override fun createVideoThumbnail(videoUri: String): Result<Bitmap> {
        return mediaUtil.extractFirstFrameFromVideoUri(videoUri)
    }

    override suspend fun addVideo(videoUri: String, videoThumbnailBitmap: Bitmap, title: String): Flow<Result<Unit>> {
        val uid = auth.currentUser?.uid

        return if (uid == null) {
            flowOf(Result.failure(Exception("오류가 발생하였습니다. 다시 로그인을 진행해주세요.")))
        } else {
            videoService.addVideo(
                videoUri = videoUri,
                videoThumbnailBitmap = videoThumbnailBitmap,
                title = title,
                userId = uid
            )
        }
    }

    override suspend fun deleteVideo(video: Video): Flow<Result<Unit>> {
        val uid = auth.currentUser?.uid

        return if (uid == null) {
            flowOf(Result.failure(Exception("오류가 발생하였습니다. 다시 로그인을 진행해주세요.")))
        } else {
            videoService.deleteVideo(video, uid)
        }
    }

    override suspend fun getAllVideoList(): Flow<Result<List<Video>>> {
        return videoService.getAllVideoList()
    }
}