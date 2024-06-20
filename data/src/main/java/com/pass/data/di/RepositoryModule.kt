package com.pass.data.di

import android.graphics.Bitmap
import com.pass.data.repository.LiveStreamingRepositoryImpl
import com.pass.data.repository.ProfileRepositoryImpl
import com.pass.data.repository.VideoRepositoryImpl
import com.pass.domain.repository.LiveStreamingRepository
import com.pass.domain.repository.ProfileRepository
import com.pass.domain.repository.VideoRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.webrtc.VideoTrack

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindsProfileRepository(repository: ProfileRepositoryImpl): ProfileRepository

    @Binds
    abstract fun bindsVideoRepository(repository: VideoRepositoryImpl): VideoRepository<Bitmap>

    @Binds
    abstract fun bindsLiveStreamingRepository(repository: LiveStreamingRepositoryImpl): LiveStreamingRepository<VideoTrack, Bitmap>
}