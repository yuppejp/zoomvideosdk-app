package com.example.zoomvideocompose

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.zoomvideocompose.ui.theme.ZoomVideoComposeTheme

@Composable
fun MutedVideoView(viewModel: ZoomViewModel, modifier: Modifier = Modifier) {
    val isJoined = viewModel.isJoined.observeAsState()
    val users = viewModel.users.observeAsState()
    val mutedUsersCount = viewModel.mutedUsersCount.observeAsState()
    val tag = "MyDebug#MutedVideoView"

    Box(modifier = modifier) {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Log.d(tag, "isJoined: ${isJoined.value}")
            Log.d(tag, "mutedUsersCount: ${mutedUsersCount.value}")
            if (isJoined.value == true && mutedUsersCount.value!! >= 0) {
                items(users.value!!) { user ->
                    Log.d(tag, "user:${user.getName()} isMuted:${user.isMuted.value}")
                    if (user.isMuted.value!!) {
                        MutedVideoItemView(user)
                    }
                }
            } else {
                // レイアウト確認用
                items(5) { item ->
                    MutedVideoItemViewDebug(item.toString())
                }
            }
        }
    }
}

@Composable
fun MutedVideoItemView(user: UserViewModel, modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        BoxWithConstraints {
            val videoHeight = maxHeight / 10 * 8
            val videoWidth = videoHeight
            val textHeight = maxHeight - videoHeight

            Column {
                Box(
                    modifier = Modifier
                        .width(videoWidth)
                        .height(videoHeight)
                        .clip(CircleShape)
                        .background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    ZoomVideoView(user.zoomUser.videoCanvas)
                }
                Text(
                    text = user.getName(),
                    style = MaterialTheme.typography.caption,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .width(videoWidth)
                        .height(textHeight)
                )
            }
        }
    }
}

@Composable
fun MutedVideoItemViewDebug(id: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        BoxWithConstraints {
            val videoHeight = maxHeight / 10 * 8
            val videoWidth = videoHeight
            val textHeight = maxHeight - videoHeight

            Column {
                Box(
                    modifier = Modifier
                        .width(videoWidth)
                        .height(videoHeight)
                        .clip(CircleShape)
                        .background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = id,
                        style = MaterialTheme.typography.caption,
                    )
                }
                Text(
                    text = "user$id",
                    style = MaterialTheme.typography.caption,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .width(videoWidth)
                        .height(textHeight)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MutedVideoViewPreview() {
    ZoomVideoComposeTheme {
        Column {
            val viewModel = ZoomViewModel()
            MutedVideoView(viewModel, modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.weight(5f))
        }
    }
}