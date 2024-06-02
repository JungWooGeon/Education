package com.pass.data.di

import android.media.MediaMetadataRetriever
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.io.ByteArrayOutputStream

@Module
@InstallIn(SingletonComponent::class)
object MediaModule {

    @Provides
    fun providesByteArrayOutputStream(): ByteArrayOutputStream {
        return ByteArrayOutputStream()
    }

    @Provides
    fun providesMediaMetadataRetriever(): MediaMetadataRetriever {
        return MediaMetadataRetriever()
    }
}