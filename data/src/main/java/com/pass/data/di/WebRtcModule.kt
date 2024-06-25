package com.pass.data.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.webrtc.Camera2Enumerator
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.EglBase
import org.webrtc.PeerConnectionFactory
import org.webrtc.VideoDecoderFactory
import org.webrtc.VideoEncoderFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WebRtcModule {

    @Singleton
    @Provides
    fun providesEglBaseContext(): EglBase.Context {
        return EglBase.create().eglBaseContext
    }

    @Singleton
    @Provides
    fun providesPeerConnectionFactory(context: Context, eglBaseContext: EglBase.Context): PeerConnectionFactory {
        PeerConnectionFactory.initialize(
            PeerConnectionFactory.InitializationOptions.builder(context)
                .setEnableInternalTracer(true)
                .createInitializationOptions()
        )

        val videoDecoderFactory: VideoDecoderFactory = DefaultVideoDecoderFactory(eglBaseContext)
        val videoEncoderFactory: VideoEncoderFactory = DefaultVideoEncoderFactory(eglBaseContext, true, true)

        return PeerConnectionFactory.builder()
            .setVideoDecoderFactory(videoDecoderFactory)
            .setVideoEncoderFactory(videoEncoderFactory)
            .createPeerConnectionFactory()
    }

    @Singleton
    @Provides
    fun providesCamera2Enumerator(context: Context): Camera2Enumerator {
        return Camera2Enumerator(context)
    }
}