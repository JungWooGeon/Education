package com.pass.data.di

import com.pass.data.manager.capture.AudioCaptureManager
import com.pass.data.manager.capture.AudioCaptureManagerImpl
import com.pass.data.manager.capture.VideoCaptureManager
import com.pass.data.manager.capture.VideoCaptureManagerImpl
import com.pass.data.manager.socket.SocketConnectionManager
import com.pass.data.manager.socket.SocketConnectionManagerImpl
import com.pass.data.manager.socket.SocketMessageManager
import com.pass.data.manager.socket.SocketMessageManagerImpl
import com.pass.data.manager.webrtc.IceCandidateManager
import com.pass.data.manager.webrtc.IceCandidateManagerImpl
import com.pass.data.manager.webrtc.PeerConnectionManager
import com.pass.data.manager.webrtc.PeerConnectionManagerImpl
import com.pass.data.manager.webrtc.SdpManager
import com.pass.data.manager.webrtc.SdpManagerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ManagerModule {

    @Singleton
    @Binds
    abstract fun bindsVideoCapture(manager: VideoCaptureManagerImpl): VideoCaptureManager

    @Singleton
    @Binds
    abstract fun bindsAudioCapture(manager: AudioCaptureManagerImpl): AudioCaptureManager

    @Singleton
    @Binds
    abstract fun bindsPeerConnectionManager(manager: PeerConnectionManagerImpl): PeerConnectionManager

    @Singleton
    @Binds
    abstract fun bindsSdpManager(manager: SdpManagerImpl): SdpManager

    @Singleton
    @Binds
    abstract fun bindsIceCandidateManger(manager: IceCandidateManagerImpl): IceCandidateManager

    @Singleton
    @Binds
    abstract fun bindsSocketConnectionManager(manager: SocketConnectionManagerImpl): SocketConnectionManager

    @Singleton
    @Binds
    abstract fun bindsSocketMessageManager(manager: SocketMessageManagerImpl): SocketMessageManager
}