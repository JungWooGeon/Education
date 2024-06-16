package com.pass.data.manager.database

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class FirebaseAuthManagerImpl @Inject constructor(
    private val auth: FirebaseAuth
) : AuthManager {
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
    ): Flow<Result<String>> = callbackFlow {
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
                            trySend(Result.success(uid))
                        }
                    } else {
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
        return auth.signOut()
    }
}