package com.pass.data.service.auth

import com.google.firebase.firestore.DocumentSnapshot
import com.pass.data.manager.database.AuthManager
import com.pass.data.manager.database.DatabaseManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class SignServiceImpl @Inject constructor(
    private val firebaseAuthManager: AuthManager,
    private val firebaseDatabaseManager: DatabaseManager<DocumentSnapshot>,
) : SignService {

    override suspend fun isSignedIn(): Flow<Boolean> {
        return firebaseAuthManager.isSignedIn()
    }

    override suspend fun signIn(id: String, password: String): Flow<Result<Unit>> {
        return firebaseAuthManager.signIn(id, password)
    }

    override suspend fun signUp(id: String, password: String, verifyPassword: String): Flow<Result<Unit>> = callbackFlow {
        firebaseAuthManager.signUp(id, password, verifyPassword).collect { result ->
            result.onSuccess { uid ->
                // create user profile in database
                val userProfile = hashMapOf(
                    "name" to uid,
                    "pictureUrl" to ""
                )

                firebaseDatabaseManager.createData(userProfile, "profiles", uid).collect {
                    trySend(it)
                }
            }.onFailure {
                trySend(Result.failure(it))
            }
        }

        awaitClose()
    }

    override suspend fun signOut() {
        return firebaseAuthManager.signOut()
    }
}