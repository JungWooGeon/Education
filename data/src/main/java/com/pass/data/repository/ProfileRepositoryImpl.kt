package com.pass.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.pass.domain.repository.ProfileRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class ProfileRepositoryImpl @Inject constructor(private val auth: FirebaseAuth) : ProfileRepository {

    override suspend fun isSignedIn(): Flow<Boolean> = callbackFlow {
        val currentUser = auth.currentUser
        trySend(currentUser != null)
        awaitClose()
    }

    override suspend fun signIn(id: String, password: String): Flow<Result<Unit>> = callbackFlow {
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
        awaitClose()
    }

    override suspend fun signUp(id: String, password: String): Flow<Result<Unit>> = callbackFlow {
        auth.createUserWithEmailAndPassword(id, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // 회원가입 성공
                trySend(Result.success(Unit))
            } else {
                // 회원가입 실패
                task.exception?.let {
                    trySend(Result.failure(it))
                } ?: trySend(Result.failure(Exception("Unknown Error")))
            }
        }
        awaitClose()
    }

    override suspend fun signOut() {
        return auth.signOut()
    }
}