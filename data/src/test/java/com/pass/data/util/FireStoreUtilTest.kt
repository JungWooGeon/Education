package com.pass.data.util

import com.google.firebase.firestore.DocumentSnapshot
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
}