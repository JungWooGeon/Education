package com.pass.data.service.auth

import com.google.firebase.firestore.DocumentSnapshot
import com.pass.data.manager.database.AuthManager
import com.pass.data.manager.database.DatabaseManager
import com.pass.data.util.FireStoreUtil
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Sign 관련 데이터 가공(생성, 수정, 추출 등) 및 비지니스 로직 구현
 */
class SignServiceImpl @Inject constructor(
    private val firebaseAuthManager: AuthManager,
    private val firebaseDatabaseManager: DatabaseManager<DocumentSnapshot>,
    private val fireStoreUtil: FireStoreUtil
) : SignService {

    override suspend fun isSignedIn(): Flow<Boolean> {
        return firebaseAuthManager.isSignedIn()
    }

    override suspend fun signIn(id: String, password: String): Flow<Result<Unit>> {
        return firebaseAuthManager.signIn(id, password)
    }

    /**
     * 1. Flow<Result<String>> : firebaseAuth 회원 가입
     * 2. User 데이터 생성
     * 3. Flow<Result<Unit>> : firestore User 데이터 추가
     */
    override suspend fun signUp(id: String, password: String, verifyPassword: String): Flow<Result<Unit>> = callbackFlow {
        val signUpFlowResult = firebaseAuthManager.signUp(id, password, verifyPassword).first()
        signUpFlowResult.onSuccess { uid ->
            val userProfile = fireStoreUtil.createUserProfileData(name = uid, pictureUrl = "")
            trySend(firebaseDatabaseManager.createData(userProfile, "profiles", uid).first())
        }.onFailure {
            trySend(Result.failure(it))
        }

        awaitClose()
    }

    override suspend fun signOut() {
        return firebaseAuthManager.signOut()
    }
}