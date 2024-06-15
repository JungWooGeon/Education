package com.pass.data.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Base64
import androidx.test.platform.app.InstrumentationRegistry
import io.mockk.mockk
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.ByteArrayOutputStream

@RunWith(RobolectricTestRunner::class)
@Config(manifest = "./src/main/AndroidManifest.xml")
class MediaUtilTest {

    private val mockContext = mockk<Context>()
    private val mockMediaMetadataRetriever = mockk< MediaMetadataRetriever>()

    private val byteArrayOutputStream = ByteArrayOutputStream()

    private val mediaUtil = MediaUtil(mockContext, mockMediaMetadataRetriever, byteArrayOutputStream)

    @Test
    fun testSuccessConvertBitmapToStringAndStringToBitmap() {
        // 임의의 Bitmap 생성
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint()
        paint.color = Color.BLUE
        canvas.drawRect(0f, 0f, 100f, 100f, paint)

        // Bitmap To String 테스트 값
        val resultBitmapToString = mediaUtil.convertBitmapToString(bitmap)

        // Bitmap To String 기대값
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        val expectedBitMapToString = Base64.encodeToString(byteArray, Base64.DEFAULT)

        // Test ConvertBitmapToString
        assertEquals(expectedBitMapToString, resultBitmapToString)

        val resultStringToBitmap = mediaUtil.convertStringToBitmap(resultBitmapToString)
        val decodedBytes = Base64.decode(expectedBitMapToString, Base64.DEFAULT)
        val expectedStringToBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)

        // Test ConvertStringToBitmap
        assertTrue(areBitmapsEqual(resultStringToBitmap, expectedStringToBitmap))
    }

    private fun areBitmapsEqual(bitmap1: Bitmap, bitmap2: Bitmap): Boolean {
        if (bitmap1.width != bitmap2.width || bitmap1.height != bitmap2.height) return false
        for (x in 0 until bitmap1.width) {
            for (y in 0 until bitmap1.height) {
                if (bitmap1.getPixel(x, y) != bitmap2.getPixel(x, y)) return false
            }
        }
        return true
    }

    @Test
    fun testSuccessConvertBitmapToByteArray() {
        // 임의의 Bitmap 생성
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint()
        paint.color = Color.BLUE
        canvas.drawRect(0f, 0f, 100f, 100f, paint)

        // Bitmap To String 테스트 값
        val resultBitmapToByteArray = mediaUtil.convertBitmapToByteArray(bitmap)

        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val expectedBitmapToByteArray = baos.toByteArray()

        assertArrayEquals(resultBitmapToByteArray, expectedBitmapToByteArray)
    }

    @Test
    fun testFailExtractFirstFrame() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val mediaMetadataRetriever = MediaMetadataRetriever()

        val extractMediaUtil = MediaUtil(context, mediaMetadataRetriever, byteArrayOutputStream)

        // 테스트용 비디오 파일을 포함하는 URI (웹 URL)
//        val videoUri = Uri.parse("https://test-videos.co.uk/vids/bigbuckbunny/mp4/h264/720/Big_Buck_Bunny_720_10s_1MB.mp4")

        val videoUri = Uri.parse("https://example.com")
        val resultVideoUri = mediaUtil.urlEncode(videoUri)

        val result = extractMediaUtil.extractFirstFrameFromVideoUri(resultVideoUri)
        assertTrue(result.isFailure)
    }

    @Test
    fun testSuccessEncodeDecode() {
        val testUri = Uri.parse("https://example.com")
        val encodeUri = mediaUtil.urlEncode(testUri)
        val decodeUri = mediaUtil.urlDecode(encodeUri)

        assertEquals(testUri.toString(), decodeUri)
    }
}