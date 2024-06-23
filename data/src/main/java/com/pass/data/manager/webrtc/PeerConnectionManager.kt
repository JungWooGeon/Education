package com.pass.data.manager.webrtc

import org.json.JSONObject
import org.webrtc.AudioTrack
import org.webrtc.PeerConnection
import org.webrtc.VideoTrack

interface PeerConnectionManager {

    fun createPeerConnection(callbackOnIceCandidate: (JSONObject) -> Unit, callbackOnReceiveVideoTrack: (VideoTrack) -> Unit, callbackOnFailure: () -> Unit)

    fun addTrackPeerConnection(videoTrack: VideoTrack, audioTrack: AudioTrack)

    fun disposePeerConnection(callbackOnFailure: () -> Unit)

    fun getPeerConnection(): PeerConnection?
}