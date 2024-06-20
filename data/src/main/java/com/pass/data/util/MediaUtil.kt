package com.pass.data.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Base64
import androidx.core.net.toUri
import com.pass.domain.util.BitmapConverter
import com.pass.domain.util.URLCodec
import java.io.ByteArrayOutputStream
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaUtil @Inject constructor(
    private val context: Context,
    private val mediaMetadataRetriever: MediaMetadataRetriever,
    private val byteArrayOutputStream: ByteArrayOutputStream
) : BitmapConverter<Bitmap>, URLCodec<Uri> {

    override fun convertBitmapToString(bitmap: Bitmap): String {
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    override fun convertStringToBitmap(base64Str: String): Bitmap {
        val decodedBytes = Base64.decode(base64Str, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    }

    fun extractFirstFrameFromVideoUri(videoUri: String): Result<Bitmap> {
        return try {
            // 미디어 파일로부터 데이터를 추출하기 위해 setDataSource를 호출
            mediaMetadataRetriever.setDataSource(context, urlDecode(videoUri).toUri())

            val thumbnailBitmap = mediaMetadataRetriever.frameAtTime

            if (thumbnailBitmap != null) {
                // 첫 번째 프레임을 비트맵으로 가져옴
                Result.success(thumbnailBitmap)
            } else {
                Result.failure(Exception("동영상 선택에 실패하였습니다."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            mediaMetadataRetriever.release()
        }
    }

    fun convertBitmapToByteArray(bitmap: Bitmap): ByteArray {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        return baos.toByteArray()
    }

    override fun urlEncode(downloadUrl: Uri): String {
        return URLEncoder.encode(
            downloadUrl.toString(),
            StandardCharsets.UTF_8.toString()
        )
    }

    override fun urlDecode(fileUri: String): String {
        return URLDecoder.decode(fileUri, StandardCharsets.UTF_8.toString())
    }
}