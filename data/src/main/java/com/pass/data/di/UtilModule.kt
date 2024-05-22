package com.pass.data.di

import com.google.firebase.firestore.DocumentSnapshot
import com.pass.data.util.FirebaseAuthUtil
import com.pass.data.util.FirebaseDatabaseUtil
import com.pass.data.util.FirebaseStorageUtil
import com.pass.domain.util.AuthUtil
import com.pass.domain.util.DatabaseUtil
import com.pass.domain.util.StorageUtil
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
    abstract fun bindsAuthUtil(util: FirebaseAuthUtil): AuthUtil

    @Singleton
    @Binds
    abstract fun bindsDatabaseUtil(util: FirebaseDatabaseUtil): DatabaseUtil<DocumentSnapshot>

    @Singleton
    @Binds
    abstract fun bindsStorageUtil(util: FirebaseStorageUtil): StorageUtil
}