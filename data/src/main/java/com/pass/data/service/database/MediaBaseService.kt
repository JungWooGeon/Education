package com.pass.data.service.database

import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.flow.Flow

interface MediaBaseService {

    suspend fun getMediaAndIdList(collectionPath: String): Flow<Result<Pair<List<DocumentSnapshot>, List<String>>>>

    suspend fun getIdList(idList: List<String>): Flow<Result<List<DocumentSnapshot>>>
}