package com.pass.domain.util

interface BitmapConverter<T> {
    fun convertBitmapToString(bitmap: T): String
    fun convertStringToBitmap(base64Str: String): T
}