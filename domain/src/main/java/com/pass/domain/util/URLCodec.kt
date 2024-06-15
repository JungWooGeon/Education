package com.pass.domain.util

interface URLCodec<T> {
    fun urlEncode(downloadUrl: T): String
    fun urlDecode(fileUri: String): String
}