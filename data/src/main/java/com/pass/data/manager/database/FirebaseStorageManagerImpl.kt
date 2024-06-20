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
import javax.inject.Inject

class FirebaseStorageManagerImpl @Inject constructor(
    private val storage: FirebaseStorage,
    private val mediaUtil: MediaUtil
) : StorageManager {

    override suspend fun updateFile(fileUri: String, pathString: String): Flow<Result<String>> =
        callbackFlow {
            val file = withContext(Dispatchers.IO) {
                mediaUtil.urlDecode(fileUri)
            }.toUri()
            storage.reference.child(pathString).putFile(file)
                .addOnSuccessListener { taskSnapshot ->
                    // DB 에 프로필 링크 업데이트
                    taskSnapshot.metadata?.reference?.downloadUrl
                        ?.addOnSuccessListener { downloadUrl ->
                            launch {
                                val uriString = withContext(Dispatchers.IO) {
                                    mediaUtil.urlEncode(downloadUrl)
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
        bitmap: Bitmap,
        pathString: String
    ): Flow<Result<String>> = callbackFlow {
        val data = mediaUtil.convertBitmapToByteArray(bitmap)

        val uploadTask = storage.reference.child(pathString).putBytes(data)
        uploadTask.addOnSuccessListener { taskSnapshot ->
            taskSnapshot.metadata?.reference?.downloadUrl
                ?.addOnSuccessListener { downloadUrl ->
                    launch {
                        val uriString = withContext(Dispatchers.IO) {
                            mediaUtil.urlEncode(downloadUrl)
                        }
                        trySend(Result.success(uriString))
                    }
                }?.addOnFailureListener {
                    trySend(Result.failure(Exception("업로드를 시도하던 중 오류가 발생하였습니다. 잠시 후 시도해주세요.")))
                }
        }.addOnFailureListener {
            trySend(Result.failure(Exception("업로드를 시도하던 중 오류가 발생하였습니다. 잠시 후 시도해주세요.")))
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