package com.pass.presentation.view.component

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.pass.presentation.R

@Composable
fun VideoListItem(
    context: Context,
    videoThumbnailUrl: String,
    videoTitle: String,
    userProfileUrl: String,
    userName: String,
    onClickVideoItem: () -> Unit,
    onClickVideoDeleteMoreIcon: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(10.dp)
            .clickable { onClickVideoItem() }
    ) {
        // video thumbnail image
        if (videoThumbnailUrl == "") {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1.5f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White)
            )
        } else {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(videoThumbnailUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                clipToBounds = true,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1.5f)
                    .clip(RoundedCornerShape(10.dp))
            )
        }

        // user profile / video title
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
                .padding(start = 10.dp, top = 10.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxHeight()
            ) {
                Row(
                    modifier = Modifier.fillMaxHeight()
                ) {
                    ProfileImageView(
                        context = context,
                        modifier = Modifier.padding(4.dp),
                        userProfileUrl = userProfileUrl,
                        imageSize = 16.dp,
                        onClickProfileImage = {}
                    )

                    Text(
                        text = userName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        modifier = Modifier.padding(start = 5.dp)
                    )
                }

                Text(
                    text = videoTitle,
                    fontSize = 12.sp,
                    lineHeight = 20.sp,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 2,
                    modifier = Modifier.padding(top = 5.dp)
                )

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "7년 전",
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 5.dp)
                )
            }
        }

        // video delete More Icon Button
        IconButton(onClick = onClickVideoDeleteMoreIcon) {
            Icon(
                painter = painterResource(id = R.drawable.ic_more_vertical),
                contentDescription = null,
                tint = Color.Black.copy(alpha = 0.8F),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}