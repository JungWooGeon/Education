package com.pass.data.util

import com.google.firebase.firestore.DocumentSnapshot
import com.pass.domain.model.Profile
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FireStoreUtilTest {

    private val mockURLCodec = mockk<MediaUtil>()
    private val mockDocumentSnapShot = mockk<DocumentSnapshot>()

    private val fireStoreUtil = FireStoreUtil(mockURLCodec)

    @Test
    fun testSuccessExtractUserIdsFromDocuments() {
        val testDocumentSnapshotList = listOf(mockDocumentSnapShot, mockDocumentSnapShot, mockDocumentSnapShot)

        every { mockDocumentSnapShot.getString(any()) } returns "test"

        val resultIdList = fireStoreUtil.extractUserIdsFromDocuments(testDocumentSnapshotList)

        assertEquals(resultIdList.size, 1)

        if (resultIdList.isNotEmpty()) {
            assertEquals(resultIdList[0], "test")
        }
    }

    @Test
    fun testSuccessExtractIdOfProfileMapFromDocuments() {
        val testDocumentSnapshotList = listOf(mockDocumentSnapShot, mockDocumentSnapShot, mockDocumentSnapShot)

        every { mockDocumentSnapShot.id } returns "testId"
        every { mockDocumentSnapShot.getString("name") } returns "testName"
        every { mockDocumentSnapShot.getString("pictureUrl") } returns "testPictureUrl"

        val resultIdOfProfileMap = fireStoreUtil.extractIdOfProfileMapFromDocuments(testDocumentSnapshotList)

        assertTrue(resultIdOfProfileMap.containsKey("testId"))
        if (resultIdOfProfileMap.containsKey("testId")) {
            assertEquals(resultIdOfProfileMap["testId"]?.name, "testName")
            assertEquals(resultIdOfProfileMap["testId"]?.pictureUrl, "testPictureUrl")
            assertEquals(resultIdOfProfileMap["testId"]?.videoList?.size, 0)
        }
    }

    @Test
    fun testSuccessExtractLiveStreamingListInfoFromIdMapAndDocuments() {
        val testDocumentSnapshotList = listOf(mockDocumentSnapShot, mockDocumentSnapShot, mockDocumentSnapShot)

        every { mockDocumentSnapShot.id } returns "testId"
        every { mockDocumentSnapShot.getString("title") } returns "testTitle"
        every { mockDocumentSnapShot.getString("startTime") } returns "testStartTime"
        every { mockDocumentSnapShot.getString("userId") } returns "testUserId"
        every { mockDocumentSnapShot.getString("time") } returns "testTime"

        val testProfile = Profile(
            name = "testName",
            pictureUrl = "testPictureUrl",
            videoList = emptyList()
        )

        val resultLiveStreamingList = fireStoreUtil.extractLiveStreamingListInfoFromIdMapAndDocuments(
            videoDocumentSnapShotList = testDocumentSnapshotList,
            idOfProfileMap = mapOf("testUserId" to testProfile),
            calculateAgoTime = { "0초 전" }
        )

        assertEquals(resultLiveStreamingList.size, 3)
        assertEquals(resultLiveStreamingList[0].broadcastId, "testId")
        assertEquals(resultLiveStreamingList[0].title, "testTitle")
        assertEquals(resultLiveStreamingList[0].userProfileURL, "testPictureUrl")
        assertEquals(resultLiveStreamingList[0].userName, "testName")
    }

    @Test
    fun testSuccessExtractProfileFromProfileAndVideoDocuments() {
        val mockProfileDocumentSnapshot = mockk<DocumentSnapshot>()
        val testVideoDocumentSnapshotList = listOf(mockDocumentSnapShot, mockDocumentSnapShot, mockDocumentSnapShot)

        every { mockProfileDocumentSnapshot.getString("name") } returns "testName"
        every { mockProfileDocumentSnapshot.getString("pictureUrl") } returns "testPictureUrl"
        every { mockDocumentSnapShot.getString("videoThumbnailUrl") } returns "testVideoThumbnailUrl"
        every { mockDocumentSnapShot.getString("videoUrl") } returns "testVideoUrl"
        every { mockDocumentSnapShot.getString("title") } returns "testTitle"
        every { mockDocumentSnapShot.getString("time") } returns "testTime"
        every { mockDocumentSnapShot.id } returns "testId"

        val result = fireStoreUtil.extractProfileFromProfileAndVideoDocuments(
            userId = "testUserId",
            calculateAgoTime = { "0초 전" },
            profileDocumentSnapshot = mockProfileDocumentSnapshot,
            videoDocumentSnapshotList = testVideoDocumentSnapshotList
        )

        assertTrue(result.isSuccess)
        assertEquals(result.getOrNull()?.name, "testName")
        assertEquals(result.getOrNull()?.pictureUrl, "testPictureUrl")
        assertEquals(result.getOrNull()?.videoList?.get(0)?.videoId ?: "", "testId")
    }

    @Test
    fun testFailExtractProfileFromProfileAndVideoDocumentsWithDocumentSnapshotNull() {
        val result = fireStoreUtil.extractProfileFromProfileAndVideoDocuments(
            userId = "testUserId",
            calculateAgoTime = { "0초 전" },
            profileDocumentSnapshot = null,
            videoDocumentSnapshotList = null
        )

        assertTrue(result.isFailure)
        assertEquals(result.exceptionOrNull()?.message, "프로필을 조회할 수 없습니다.")
    }

    @Test
    fun testFailExtractProfileFromProfileAndVideoDocumentsWithNameOrPictureUrlNull() {
        val mockProfileDocumentSnapshot = mockk<DocumentSnapshot>()
        val testVideoDocumentSnapshotList = listOf(mockDocumentSnapShot, mockDocumentSnapShot, mockDocumentSnapShot)

        every { mockProfileDocumentSnapshot.getString("name") } returns null
        every { mockProfileDocumentSnapshot.getString("pictureUrl") } returns "testPictureUrl"
        every { mockDocumentSnapShot.getString("videoThumbnailUrl") } returns "testVideoThumbnailUrl"
        every { mockDocumentSnapShot.getString("videoUrl") } returns "testVideoUrl"
        every { mockDocumentSnapShot.getString("title") } returns "testTitle"
        every { mockDocumentSnapShot.getString("time") } returns "testTime"
        every { mockDocumentSnapShot.id } returns "testId"

        val result = fireStoreUtil.extractProfileFromProfileAndVideoDocuments(
            userId = "testUserId",
            calculateAgoTime = { "0초 전" },
            profileDocumentSnapshot = mockProfileDocumentSnapshot,
            videoDocumentSnapshotList = testVideoDocumentSnapshotList
        )

        assertTrue(result.isFailure)
        assertEquals(result.exceptionOrNull()?.message, "name or picture null")
    }

    @Test
    fun testSuccessExtractVideoListInfoFromIdMapAndDocuments() {
        val testVideoDocumentSnapshotList = listOf(mockDocumentSnapShot, mockDocumentSnapShot, mockDocumentSnapShot)

        val testProfile = Profile(
            name = "testName",
            pictureUrl = "testPictureUrl",
            videoList = emptyList()
        )

        every { mockDocumentSnapShot.id } returns "testId"
        every { mockDocumentSnapShot.getString("userId") } returns "testUserId"
        every { mockDocumentSnapShot.getString("title") } returns "testTitle"
        every { mockDocumentSnapShot.getString("videoThumbnailUrl") } returns "testVideoThumbnailUrl"
        every { mockDocumentSnapShot.getString("videoUrl") } returns "testVideoUrl"
        every { mockDocumentSnapShot.getString("time") } returns "testTime"

        val result = fireStoreUtil.extractVideoListInfoFromIdMapAndDocuments(
            videoDocumentSnapShotList = testVideoDocumentSnapshotList,
            idOfProfileMap = mapOf("testUserId" to testProfile),
            calculateAgoTime = { "0초 전" }
        )

        assertEquals(result.size, 3)
        assertEquals(result[0].videoId, "testId")
        assertEquals(result[0].userId, "testUserId")
        assertEquals(result[0].videoThumbnailUrl, "testVideoThumbnailUrl")
        assertEquals(result[0].videoTitle, "testTitle")
        assertEquals(result[0].agoTime, "0초 전")
        assertEquals(result[0].videoUrl, "testVideoUrl")
        assertEquals(result[0].userName, "testName")
        assertEquals(result[0].userProfileUrl, "testPictureUrl")
    }

    @Test
    fun testSuccessCreateBroadcastData() {
        val result = fireStoreUtil.createBroadcastData(
            broadcastId = "testBroadcastId",
            title = "testTitle",
            startTime = "testStartTime",
            liveThumbnailUri = "testThumbnail"
        )

        assertEquals(result["userId"], "testBroadcastId")
        assertEquals(result["title"], "testTitle")
        assertEquals(result["startTime"], "testStartTime")
    }

    @Test
    fun testSuccessCreateUserProfileData() {
        val result = fireStoreUtil.createUserProfileData(
            name = "testName",
            pictureUrl = "testPictureUrl"
        )

        assertEquals(result["name"], "testName")
        assertEquals(result["pictureUrl"], "testPictureUrl")
    }

    @Test
    fun testSuccessCreateVideoData() {
        every { mockURLCodec.urlDecode(any()) } returns "testVideoThumbnailUri"

        val result = fireStoreUtil.createVideoData(
            title = "testTitle",
            userId = "testUserId",
            videoThumbnailUri = "testVideoThumbnailUri",
            videoUrl = "testVideoUrl",
            time = "testTime"
        )

        assertEquals(result["title"], "testTitle")
        assertEquals(result["userId"], "testUserId")
        assertEquals(result["videoThumbnailUrl"], "testVideoThumbnailUri")
        assertEquals(result["videoUrl"], "testVideoUrl")
        assertEquals(result["time"], "testTime")
    }

    @Test
    fun testSuccessCreateProfileFromDocumentSnapShot() {
        every { mockDocumentSnapShot.getString("name") } returns "testName"
        every { mockDocumentSnapShot.getString("pictureUrl") } returns "testPictureUrl"

        val result = fireStoreUtil.createProfileFromDocumentSnapShot(mockDocumentSnapShot)

        assertTrue(result.isSuccess)
        assertEquals(result.getOrNull()?.name, "testName")
        assertEquals(result.getOrNull()?.pictureUrl, "testPictureUrl")
        assertEquals(result.getOrNull()?.videoList?.size, 0)
    }

    @Test
    fun testFailCreateProfileFromDocumentSnapshot() {
        every { mockDocumentSnapShot.getString("name") } returns null
        every { mockDocumentSnapShot.getString("pictureUrl") } returns "testPictureUrl"

        val result = fireStoreUtil.createProfileFromDocumentSnapShot(mockDocumentSnapShot)

        assertTrue(result.isFailure)
        assertEquals(result.exceptionOrNull()?.message, "프로필을 조회할 수 없습니다.")
    }
}