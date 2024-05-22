package com.pass.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.util.Base64
import androidx.core.net.toUri
import com.pass.domain.repository.VideoRepository
import java.io.ByteArrayOutputStream
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import javax.inject.Inject

class VideoRepositoryImpl @Inject constructor(private val context: Context): VideoRepository {
    override fun createVideoThumbnail(videoUri: String): Result<String> {
        val retriever = MediaMetadataRetriever()

        try {
            // 미디어 파일로부터 데이터를 추출하기 위해 setDataSource를 호출
            retriever.setDataSource(context, URLDecoder.decode(videoUri, StandardCharsets.UTF_8.toString()).toUri())

            // 첫 번째 프레임을 비트맵으로 가져옴
            val thumbnailBitmap = retriever.frameAtTime

            return if (thumbnailBitmap == null) {
                Result.failure(Exception("동영상 선택에 실패하였습니다."))
            } else {
                Result.success(convertBitmapToString(thumbnailBitmap))
            }
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            return Result.failure(Exception("동영상 선택에 실패하였습니다."))
        } finally {
            // MediaMetadataRetriever 인스턴스를 해제
            retriever.release()
        }
    }

    private fun convertBitmapToString(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }
}