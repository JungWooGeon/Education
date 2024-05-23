package com.pass.data.util

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.pass.domain.util.DatabaseUtil
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class FirebaseDatabaseUtil @Inject constructor(
    private val auth: FirebaseAuth,
    private val fireStore: FirebaseFirestore
) : DatabaseUtil<DocumentSnapshot> {

    override suspend fun deleteData(
        collectionPath: String,
        documentPath: String,
        collectionPath2: String?,
        documentPath2: String?
    ): Flow<Result<Unit>> = callbackFlow {
        if (collectionPath2 != null && documentPath2 != null) {
            fireStore.collection(collectionPath).document(documentPath)
                .collection(collectionPath2).document(documentPath2)
                .delete()
                .addOnSuccessListener {
                    trySend(Result.success(Unit))
                }
                .addOnFailureListener { e ->
                    trySend(Result.failure(e))
                }
        } else {
            fireStore.collection(collectionPath).document(documentPath)
                .delete()
                .addOnSuccessListener {
                    trySend(Result.success(Unit))
                }
                .addOnFailureListener { e ->
                    trySend(Result.failure(e))
                }
        }

        awaitClose()
    }

    override suspend fun readData(
        collectionPath: String,
        documentPath: String
    ) = callbackFlow {
        fireStore.collection(collectionPath).document(documentPath)
            .get()
            .addOnSuccessListener { document ->
                trySend(Result.success(document))
            }
            .addOnFailureListener { e ->
                trySend(Result.failure<DocumentSnapshot>(e))
            }

        awaitClose()
    }

    override suspend fun createData(
        dataMap: HashMap<String, String>,
        collectionPath: String,
        documentPath: String,
        collectionPath2: String?,
        documentPath2: String?
    ): Flow<Result<Unit>> = callbackFlow {

        if (collectionPath2 != null && documentPath2 != null) {
            fireStore.collection(collectionPath).document(documentPath)
                .collection(collectionPath2).document(documentPath2)
                .set(dataMap)
                .addOnSuccessListener {
                    trySend(Result.success(Unit))
                }
                .addOnFailureListener { e ->
                    trySend(Result.failure(Exception(e.message)))
                }
        } else {
            fireStore.collection(collectionPath).document(documentPath)
                .set(dataMap)
                .addOnSuccessListener {
                    trySend(Result.success(Unit))
                }
                .addOnFailureListener { e ->
                    trySend(Result.failure(Exception(e.message)))
                }
        }

        awaitClose()
    }

    override suspend fun updateData(
        name: String,
        field: String,
        collectionPath: String
    ): Flow<Result<Unit>> = callbackFlow {
        val userId = auth.currentUser?.uid

        if (userId != null) {
            fireStore.runTransaction { transaction ->
                transaction.update(
                    fireStore.collection(collectionPath).document(userId),
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

    override suspend fun readDataList(
        collectionPath: String,
        documentPath: String,
        collectionPath2: String?
    ): Flow<Result<List<DocumentSnapshot>>> = callbackFlow {

        if (collectionPath2 != null) {
            fireStore.collection(collectionPath)
                .document(documentPath)
                .collection(collectionPath2)
                .get()
                .addOnSuccessListener { result ->
                    trySend(Result.success(result.documents))

                }.addOnFailureListener { e ->
                    trySend(Result.failure(e))
                }
        } else {
            fireStore.collection(collectionPath)
                .get()
                .addOnSuccessListener { result ->
                    trySend(Result.success(result.documents))
                }.addOnFailureListener { e ->
                    trySend(Result.failure(e))
                }
        }

        awaitClose()
    }
}