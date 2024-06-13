package com.pass.data.di

import com.pass.data.service.auth.SignService
import com.pass.data.service.auth.SignServiceImpl
import com.pass.data.service.database.LiveStreamingService
import com.pass.data.service.database.LiveStreamingServiceImpl
import com.pass.data.service.database.UserService
import com.pass.data.service.database.UserServiceImpl
import com.pass.data.service.webrtc.WebRtcBaseService
import com.pass.data.service.webrtc.WebRtcBaseServiceImpl
import com.pass.data.service.webrtc.WebRtcBroadCasterService
import com.pass.data.service.webrtc.WebRtcBroadCasterServiceImpl
import com.pass.data.service.webrtc.WebRtcViewerService
import com.pass.data.service.webrtc.WebRtcViewerServiceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ServiceModule {

    @Singleton
    @Binds
    abstract fun bindsWebRtcBaseService(service: WebRtcBaseServiceImpl): WebRtcBaseService

    @Singleton
    @Binds
    abstract fun bindsWebRtcBroadCasterService(service: WebRtcBroadCasterServiceImpl): WebRtcBroadCasterService

    @Singleton
    @Binds
    abstract fun bindsWebRtcViewerService(service: WebRtcViewerServiceImpl): WebRtcViewerService

    @Singleton
    @Binds
    abstract fun bindsLiveStreamingService(service: LiveStreamingServiceImpl): LiveStreamingService

    @Singleton
    @Binds
    abstract fun bindsSignService(service: SignServiceImpl): SignService

    @Singleton
    @Binds
    abstract fun bindsUserService(service: UserServiceImpl): UserService
}