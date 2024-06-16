package com.pass.data.util

import android.net.Uri
import com.google.firebase.firestore.DocumentSnapshot
import com.pass.domain.model.LiveStreaming
import com.pass.domain.model.Profile
import com.pass.domain.model.Video
import com.pass.domain.util.URLCodec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FireStoreUtil @Inject constructor(
    private val urlCodec: URLCodec<Uri>
) {

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
        // 결과 livestreaming list
        val resultLiveStreamingList = mutableListOf<LiveStreaming>()

        // user 정보를 포함하여 livestreaming 정보 반영
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

    fun extractVideoListInfoFromIdMapAndDocuments(videoDocumentSnapShotList: List<DocumentSnapshot>, idOfProfileMap: Map<String, Profile>, calculateAgoTime: (String?) -> String): List<Video> {
        // 결과 video list
        val resultVideoList = mutableListOf<Video>()

        // user 정보를 포함하여 video 정보 반영
        videoDocumentSnapShotList.forEach { videoDocumentSnapShot ->
            val videoId = videoDocumentSnapShot.id
            val userId = videoDocumentSnapShot.getString("userId")
            val title = videoDocumentSnapShot.getString("title")
            val videoThumbnailUrl = videoDocumentSnapShot.getString("videoThumbnailUrl")
            val videoUrl = videoDocumentSnapShot.getString("videoUrl")
            val userName = idOfProfileMap[userId]?.name
            val userProfileUrl = idOfProfileMap[userId]?.pictureUrl

            val time = videoDocumentSnapShot.getString("time")
            val agoTime = calculateAgoTime(time)

            if (userId != null && title != null && videoThumbnailUrl != null && videoUrl != null) {
                resultVideoList.add(
                    Video(
                        videoId = videoId,
                        userId = userId,
                        videoThumbnailUrl = videoThumbnailUrl,
                        videoTitle = title,
                        agoTime = agoTime,
                        videoUrl = videoUrl,
                        userName = userName,
                        userProfileUrl = userProfileUrl
                    )
                )
            }
        }

        return resultVideoList
    }

    fun createBroadcastData(broadcastId: String, title: String, startTime: String): HashMap<String, String> {
        return hashMapOf(
            "userId" to broadcastId,
            "title" to title,
            "startTime" to startTime
        )
    }

    fun createUserProfileData(name: String, pictureUrl: String): HashMap<String, String> {
        return hashMapOf(
            "name" to name,
            "pictureUrl" to pictureUrl
        )
    }

    fun createVideoData(title: String, userId: String, videoThumbnailUri: String, videoUrl: String, time: String): HashMap<String, String> {
        return hashMapOf(
            "title" to title,
            "userId" to userId,
            "videoThumbnailUrl" to urlCodec.urlDecode(videoThumbnailUri),
            "videoUrl" to videoUrl,
            "time" to time
        )
    }

    fun createProfileFromDocumentSnapShot(documentSnapShot: DocumentSnapshot): Result<Profile> {
        val name = documentSnapShot.getString("name")
        val pictureUrl = documentSnapShot.getString("pictureUrl")

        return if (name != null && pictureUrl != null) {
            val profile = Profile(
                name = name,
                pictureUrl = pictureUrl,
                videoList = emptyList()
            )

            Result.success(profile)
        } else {
            Result.failure(Exception("프로필을 조회할 수 없습니다."))
        }
    }
}