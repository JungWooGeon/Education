package com.pass.data.manager.webrtc

interface SdpManager {

    fun createOfferPeerConnection(callbackOnSetSuccess: (String?) -> Unit, callbackOnFailure: () -> Unit)

    fun createAnswerPeerConnection(callbackOnSetSuccess: (String?) -> Unit, callbackOnFailure: () -> Unit)
}