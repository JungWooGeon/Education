package com.pass.data.di

import com.pass.data.repository.ProfileRepositoryImpl
import com.pass.data.repository.VideoRepositoryImpl
import com.pass.domain.repository.ProfileRepository
import com.pass.domain.repository.VideoRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindsProfileRepository(repository: ProfileRepositoryImpl): ProfileRepository

    @Binds
    abstract fun bindsVideoRepository(repository: VideoRepositoryImpl): VideoRepository
}