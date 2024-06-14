package com.pass.domain.util

interface BitmapConverter<T> {
    fun convertBitmapToString(bitmap: T): String
}