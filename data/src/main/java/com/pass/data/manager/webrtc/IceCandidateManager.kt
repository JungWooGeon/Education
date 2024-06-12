package com.pass.data.manager.webrtc

import org.json.JSONObject
import org.webrtc.SessionDescription

interface IceCandidateManager {

    fun setRemoteDescriptionPeerConnection(json: JSONObject, sessionDescriptionType: SessionDescription.Type, callbackOnSetSuccess: () -> Unit, callbackOnFailure: () -> Unit)

    fun addIceCandidatePeerConnection(json: JSONObject)
}