package com.pass.data.manager.database

import android.graphics.Bitmap
import androidx.core.net.toUri
import com.google.firebase.storage.FirebaseStorage
import com.pass.data.util.MediaUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import javax.inject.Inject

class FirebaseStorageManagerImpl @Inject constructor(
    private val storage: FirebaseStorage,
    private val mediaUtil: MediaUtil
) : StorageManager {

    override suspend fun updateFile(fileUri: String, pathString: String): Flow<Result<String>> =
        callbackFlow {
            val file = withContext(Dispatchers.IO) {
                URLDecoder.decode(fileUri, StandardCharsets.UTF_8.toString())
            }.toUri()
            storage.reference.child(pathString).putFile(file)
                .addOnSuccessListener { taskSnapshot ->
                    // DB 에 프로필 링크 업데이트
                    taskSnapshot.metadata?.reference?.downloadUrl
                        ?.addOnSuccessListener { downloadUrl ->
                            launch {
                                val uriString = withContext(Dispatchers.IO) {
                                    URLEncoder.encode(
                                        downloadUrl.toString(),
                                        StandardCharsets.UTF_8.toString()
                                    )
                                }
                                trySend(Result.success(uriString))
                            }
                        }?.addOnFailureListener {
                            trySend(Result.failure(Exception("프로필 사진 업로드를 시도하던 중 오류가 발생하였습니다. 잠시 후 시도해주세요.")))
                        }
                }.addOnFailureListener {
                    trySend(Result.failure(Exception("프로필 사진 업로드를 시도하던 중 오류가 발생하였습니다. 잠시 후 시도해주세요.")))
                }

            awaitClose()
        }

    override suspend fun updateFileWithBitmap(
        bitmapString: String,
        videoId: String
    ): Flow<Result<String>> = callbackFlow {
        val bitmap = mediaUtil.convertStringToBitmap(bitmapString)
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        val uploadTask = storage.reference.child("video_thumbnail/${videoId}").putBytes(data)
        uploadTask.addOnSuccessListener { taskSnapshot ->
            taskSnapshot.metadata?.reference?.downloadUrl
                ?.addOnSuccessListener { downloadUrl ->
                    launch {
                        val uriString = withContext(Dispatchers.IO) {
                            URLEncoder.encode(
                                downloadUrl.toString(),
                                StandardCharsets.UTF_8.toString()
                            )
                        }
                        trySend(Result.success(uriString))
                    }
                }?.addOnFailureListener {
                    trySend(Result.failure(Exception("동영상 업로드를 시도하던 중 오류가 발생하였습니다. 잠시 후 시도해주세요.")))
                }
        }.addOnFailureListener {
            trySend(Result.failure(Exception("동영상 업로드를 시도하던 중 오류가 발생하였습니다. 잠시 후 시도해주세요.")))
        }

        awaitClose()
    }

    override suspend fun deleteFile(pathString: String): Flow<Result<Unit>> = callbackFlow {
        storage.reference.child(pathString)
            .delete()
            .addOnSuccessListener {
                trySend(Result.success(Unit))
            }.addOnFailureListener { e ->
                trySend(Result.failure(e))
            }

        awaitClose()
    }
}