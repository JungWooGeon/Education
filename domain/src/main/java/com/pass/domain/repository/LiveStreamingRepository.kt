package com.pass.domain.repository

import com.pass.domain.model.LiveStreaming

interface LiveStreamingRepository {
    fun getLiveStreamingListUseCase(): Result<LiveStreaming>
}