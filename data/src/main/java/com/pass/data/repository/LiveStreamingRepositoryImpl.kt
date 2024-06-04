package com.pass.data.repository

import com.pass.data.util.WebRtcUtil
import com.pass.domain.model.LiveStreaming
import com.pass.domain.repository.LiveStreamingRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class LiveStreamingRepositoryImpl @Inject constructor(
    private val webRtcUtil: WebRtcUtil
) : LiveStreamingRepository {
    override suspend fun getLiveStreamingListUseCase(): Flow<Result<List<LiveStreaming>>> {
        TODO("Not yet implemented")
    }

    override suspend fun startLiveStreamingUseCase(): Flow<Result<Boolean>> = callbackFlow {
        try {
            webRtcUtil.initializePeerConnectionFactory()
            webRtcUtil.createPeerConnection()
            webRtcUtil.createLocalMediaStream()

            awaitClose { /* Clean up resources if needed */ }
            trySend(Result.success(true))
        } catch (e: Exception) {
            trySend(Result.failure(e))
        }

        awaitClose()
    }
}