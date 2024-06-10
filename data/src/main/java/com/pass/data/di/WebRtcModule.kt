package com.pass.data.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.webrtc.EglBase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WebRtcModule {

    @Singleton
    @Provides
    fun providesEglBaseContext(): EglBase.Context {
        return EglBase.create().eglBaseContext
    }
}