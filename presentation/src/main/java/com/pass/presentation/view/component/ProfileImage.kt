package com.pass.presentation.view.component

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Composable
fun ProfileImage(
    context: Context,
    modifier: Modifier,
    userProfileUrl: String,
    imageSize: Dp,
    onClickProfileImage: () -> Unit
) {
    if (userProfileUrl == "") {
        Box(
            modifier = modifier
                .size(imageSize)
                .clip(CircleShape)
                .background(Color.White)
                .border(
                    width = 1.dp,
                    shape = CircleShape,
                    color = Color.LightGray
                )
                .clickable { onClickProfileImage() }
        )
    } else {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(userProfileUrl)
                .crossfade(true)
                .build(),
            contentDescription = null,
            clipToBounds = true,
            contentScale = ContentScale.Crop,
            modifier = modifier
                .size(imageSize)
                .clip(CircleShape)
                .border(
                    width = 1.dp,
                    shape = CircleShape,
                    color = Color.LightGray
                )
                .clickable { onClickProfileImage() }
        )
    }
}