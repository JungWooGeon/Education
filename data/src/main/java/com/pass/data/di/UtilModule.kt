package com.pass.data.di

import android.graphics.Bitmap
import android.net.Uri
import com.pass.data.util.MediaUtil
import com.pass.domain.util.BitmapConverter
import com.pass.domain.util.URLCodec
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class UtilModule {

    @Singleton
    @Binds
    abstract fun bindsBitmapConverter(util: MediaUtil): BitmapConverter<Bitmap>

    @Singleton
    @Binds
    abstract fun bindsURLCodec(util: MediaUtil): URLCodec<Uri>
}