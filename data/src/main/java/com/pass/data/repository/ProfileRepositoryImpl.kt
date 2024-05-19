package com.pass.data.repository

import androidx.core.net.toUri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.pass.domain.model.Profile
import com.pass.domain.repository.ProfileRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class ProfileRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val fireStore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : ProfileRepository {

    override suspend fun isSignedIn(): Flow<Boolean> = callbackFlow {
        val currentUser = auth.currentUser
        trySend(currentUser != null)
        awaitClose()
    }

    override suspend fun signIn(id: String, password: String): Flow<Result<Unit>> = callbackFlow {
        if (id == "" || password == "") {
            trySend(Result.failure(Exception("아이디와 비밀번호를 입력해주세요.")))
        } else {
            auth.signInWithEmailAndPassword(id, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // 로그인 성공
                    trySend(Result.success(Unit))
                } else {
                    // 로그인 실패
                    task.exception?.let {
                        trySend(Result.failure(it))
                    } ?: trySend(Result.failure(Exception("Unknown Error")))
                }
            }
        }

        awaitClose()
    }

    override suspend fun signUp(
        id: String,
        password: String,
        verifyPassword: String
    ): Flow<Result<Unit>> = callbackFlow {
        if (id == "" || password == "" || verifyPassword == "") {
            trySend(Result.failure(Exception("아이디와 비밀번호를 입력해주세요.")))
        } else if (password != verifyPassword) {
            trySend(Result.failure(Exception("비밀번호가 맞지 않습니다.")))
        } else {
            auth.createUserWithEmailAndPassword(id, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // 회원가입 성공
                    val uid = auth.currentUser?.uid

                    if (uid != null) {
                        launch {
                            createUserProfile(uid).collect {
                                if (it.isFailure) {
                                    // 사용자 프로필 생성 실패 시
                                    signOut()
                                    deleteUser()
                                }
                                trySend(it)
                            }
                        }
                    } else {
                        launch {
                            signOut()
                            deleteUser()
                        }
                        trySend(Result.failure(Exception("사용자 ID를 얻을 수 없습니다.")))
                    }
                } else {
                    // 회원가입 실패
                    task.exception?.let {
                        trySend(Result.failure(it))
                    } ?: trySend(Result.failure(Exception("Unknown Error")))
                }
            }
        }

        awaitClose()
    }

    override suspend fun signOut() {
        auth.signOut()
    }

    override suspend fun getUserProfile(): Flow<Result<Profile>> = callbackFlow {
        val userId = auth.currentUser?.uid

        if (userId != null) {
            fireStore.collection("profiles").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    val user = document.toObject(Profile::class.java)
                    if (user != null) {
                        trySend(Result.success(user))
                    } else {
                        trySend(Result.failure(Exception("프로필을 조회할 수 없습니다.")))
                    }
                }
                .addOnFailureListener { e ->
                    trySend(Result.failure(Exception(e.message)))
                }
        } else {
            trySend(Result.failure(Exception("오류가 발생하였습니다. 다시 로그인을 진행해주세요.")))
        }
        awaitClose()
    }

    private suspend fun createUserProfile(userId: String): Flow<Result<Unit>> = callbackFlow {
        val userProfile = hashMapOf(
            "name" to userId,
            "pictureUrl" to ""
        )

        fireStore.collection("profiles").document(userId)
            .set(userProfile)
            .addOnSuccessListener {
                trySend(Result.success(Unit))
            }
            .addOnFailureListener { e ->
                trySend(Result.failure(Exception(e.message)))
            }

        awaitClose()
    }

    private fun deleteUser() {
        val user = auth.currentUser
        user?.delete()
    }

    override suspend fun updateUserProfile(name: String, field: String): Flow<Result<Unit>> =
        callbackFlow {
            val userId = auth.currentUser?.uid

            if (userId != null) {
                fireStore.runTransaction { transaction ->
                    transaction.update(
                        fireStore.collection("profiles").document(userId),
                        field,
                        name
                    )
                    null
                }.addOnSuccessListener {
                    trySend(Result.success(Unit))
                }.addOnFailureListener { e ->
                    trySend(Result.failure(Exception(e.message)))
                }
            } else {
                trySend(Result.failure(Exception("오류가 발생하였습니다. 다시 로그인을 진행해주세요.")))
            }

            awaitClose()
        }

    override suspend fun updateUserProfilePicture(pictureUri: String): Flow<Result<String>> =
        callbackFlow {
            // 기존 사진 삭제 + 새로운 사진 업로드 + 새로운 사진 url 업데이트
            val file = pictureUri.toUri()
            storage.reference.child("user_profile/${auth.currentUser?.uid}").putFile(file)
                .addOnSuccessListener { taskSnapshot ->
                    // DB 에 프로필 링크 업데이트
                    taskSnapshot.metadata?.reference?.downloadUrl
                        ?.addOnSuccessListener { downloadUrl ->
                            launch {
                                updateUserProfile(downloadUrl.toString(), "pictureUrl").collect {
                                    it.onSuccess {
                                        trySend(Result.success(downloadUrl.toString()))
                                    }.onFailure {
                                        trySend(Result.failure(Exception("프로필 사진 업로드를 시도하던 중 오류가 발생하였습니다. 잠시 후 시도해주세요.")))
                                    }
                                }
                            }
                        }?.addOnFailureListener {
                            trySend(Result.failure(Exception("프로필 사진 업로드를 시도하던 중 오류가 발생하였습니다. 잠시 후 시도해주세요.")))
                        }
                }.addOnFailureListener {
                    trySend(Result.failure(Exception("프로필 사진 업로드를 시도하던 중 오류가 발생하였습니다. 잠시 후 시도해주세요.")))
                }

            awaitClose()
        }
}