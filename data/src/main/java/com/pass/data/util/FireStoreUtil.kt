package com.pass.data.util

import com.google.firebase.firestore.DocumentSnapshot
import com.pass.domain.model.LiveStreaming
import com.pass.domain.model.Profile
import com.pass.domain.model.Video
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FireStoreUtil @Inject constructor() {

    fun extractUserIdsFromDocuments(videoDocumentSnapShotList: List<DocumentSnapshot>): List<String> {
        val idSetList = mutableSetOf<String>()
        videoDocumentSnapShotList.forEach { videoDocumentSnapShot ->
            val userId = videoDocumentSnapShot.getString("userId")
            userId?.let { idSetList.add(it) }
        }

        return idSetList.toList()
    }

    fun extractIdOfProfileMapFromDocuments(readIdDocumentSnapShotList: List<DocumentSnapshot>): Map<String, Profile> {
        // id에 대한 프로필 정보 변수
        val idOfVideoMap = mutableMapOf<String, Profile>()

        // 어떤 id 목록이 있는지 조회
        readIdDocumentSnapShotList.forEach { readIdDocumentSnapshot ->
            val userId = readIdDocumentSnapshot.id
            val name = readIdDocumentSnapshot.getString("name")
            val pictureUrl = readIdDocumentSnapshot.getString("pictureUrl")

            if (name != null && pictureUrl != null) {
                idOfVideoMap[userId] = Profile(name, pictureUrl, emptyList())
            }
        }

        return idOfVideoMap
    }

    fun extractLiveStreamingListInfoFromIdMapAndDocuments(videoDocumentSnapShotList: List<DocumentSnapshot>, idOfProfileMap: Map<String, Profile>, calculateAgoTime: (String?) -> String): List<LiveStreaming> {
        // 결과 video list
        val resultLiveStreamingList = mutableListOf<LiveStreaming>()

        // user 정보를 포함하여 video 정보 반영
        videoDocumentSnapShotList.forEach { videoDocumentSnapShot ->
            val broadcastId = videoDocumentSnapShot.id
            val title = videoDocumentSnapShot.getString("title")
            val startTime = videoDocumentSnapShot.getString("startTime")
            val userId = videoDocumentSnapShot.getString("userId")
            val userName = idOfProfileMap[userId]?.name
            val userProfileUrl = idOfProfileMap[userId]?.pictureUrl
            val time = videoDocumentSnapShot.getString("time")
            val agoTime = calculateAgoTime(time)
            // TODO ThumbnailUrl 추가

            if (userId != null && title != null && userProfileUrl != null && userName != null) {
                resultLiveStreamingList.add(
                    LiveStreaming(
                        broadcastId = broadcastId,
                        thumbnailURL = "",
                        title = title,
                        userProfileURL = userProfileUrl,
                        userName = userName
                    )
                )
            }
        }

        return resultLiveStreamingList
    }

    fun extractProfileFromProfileAndVideoDocuments(userId: String, calculateAgoTime: (String?) -> String, profileDocumentSnapshot: DocumentSnapshot?, videoDocumentSnapshotList: List<DocumentSnapshot>?): Result<Profile> {
        return if (profileDocumentSnapshot == null || videoDocumentSnapshotList == null) {
            Result.failure(Exception("프로필을 조회할 수 없습니다."))
        } else {
            val name = profileDocumentSnapshot.getString("name")
            val pictureUrl = profileDocumentSnapshot.getString("pictureUrl")
            val videoList = mutableListOf<Video>()

            videoDocumentSnapshotList.forEach { videoDocumentSnapshot ->
                val videoThumbnailUrl =
                    videoDocumentSnapshot.getString("videoThumbnailUrl")
                val videoUrl = videoDocumentSnapshot.getString("videoUrl")
                val videoTitle = videoDocumentSnapshot.getString("title")

                val agoTime = calculateAgoTime(videoDocumentSnapshot.getString("time"))

                if (videoThumbnailUrl != null && videoUrl != null && videoTitle != null) {
                    videoList.add(
                        Video(
                            videoId = videoDocumentSnapshot.id,
                            userId = userId,
                            videoThumbnailUrl = videoThumbnailUrl,
                            agoTime = agoTime,
                            videoTitle = videoTitle,
                            videoUrl = videoUrl
                        )
                    )
                }
            }

            if (name != null && pictureUrl != null) {
                val profile = Profile(
                    name = name,
                    pictureUrl = pictureUrl,
                    videoList = videoList
                )

                Result.success(profile)
            } else {
                Result.failure(Exception("name or picture null"))
            }

        }
    }
}