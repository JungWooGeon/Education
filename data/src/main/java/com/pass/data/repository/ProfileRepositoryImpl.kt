package com.pass.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.pass.domain.model.Profile
import com.pass.domain.repository.ProfileRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class ProfileRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val fireStore: FirebaseFirestore
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
                                it.onSuccess {
                                    // 사용자 프로필 생성 성공
                                    trySend(Result.success(Unit))
                                }.onFailure { e ->
                                    // 사용자 프로필 생성 실패
                                    trySend(Result.failure(Exception(e)))
                                    signOut()
                                    deleteUser()
                                }
                            }
                        }
                    } else {
                        trySend(Result.failure(Exception("사용자 ID를 얻을 수 없습니다.")))
                        launch {
                            signOut()
                            deleteUser()
                        }
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
            val docRef = fireStore.collection("profiles").document(userId)
            docRef.get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val user = document.toObject(Profile::class.java)
                        if (user != null) {
                            trySend(Result.success(user))
                        } else {
                            trySend(Result.failure(Exception("프로필을 조회할 수 없습니다.")))
                        }
                    } else {
                        trySend(Result.failure(Exception("프로필을 조회할 수 없습니다.")))
                    }
                }
                .addOnFailureListener { e ->
                    trySend(Result.failure(Exception(e.message)))
                }
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
}