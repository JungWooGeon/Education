package com.pass.data.di

import android.graphics.Bitmap
import com.pass.data.util.MediaUtil
import com.pass.domain.util.BitmapConverter
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
}