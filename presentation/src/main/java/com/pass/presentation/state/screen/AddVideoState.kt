package com.pass.presentation.state.screen

import android.graphics.Bitmap
import com.simform.ssjetpackcomposeprogressbuttonlibrary.SSButtonState
import javax.annotation.concurrent.Immutable

@Immutable
data class AddVideoState(
    val videoThumbnailBitmap: Bitmap?,
    val videoUri: String,
    val progressButtonState: SSButtonState
)