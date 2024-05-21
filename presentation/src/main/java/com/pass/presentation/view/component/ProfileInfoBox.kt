package com.pass.presentation.view.component

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pass.presentation.R

@Composable
fun ProfileInfoBox(
    context: Context,
    userProfileUrl: String,
    userName: String,
    onClickProfileImage: () -> Unit,
    onClickSignOut: () -> Unit,
    onClickEditButton: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(top = 10.dp, start = 20.dp, end = 10.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.Center
        ) {
            ProfileImage(
                context = context,
                modifier = Modifier,
                userProfileUrl = userProfileUrl,
                imageSize = 60.dp,
                onClickProfileImage = onClickProfileImage
            )

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.Start,
                    modifier = Modifier.padding(start = 16.dp).weight(1f)
                ) {
                    Text(
                        text = userName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 8.dp, end = 10.dp)
                    )

                    Text(
                        text = "Sign Out",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .padding(top = 10.dp)
                            .clickable { onClickSignOut() }
                    )
                }

                IconButton(
                    onClick = onClickEditButton
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_edit),
                        contentDescription = null,
                        tint = Color.Black.copy(alpha = 0.8F),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}