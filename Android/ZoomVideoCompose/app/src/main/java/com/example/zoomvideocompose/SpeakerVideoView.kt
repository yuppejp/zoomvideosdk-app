package com.example.zoomvideocompose

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.zoomvideocompose.ui.theme.ZoomVideoComposeTheme
import java.lang.Math.ceil
import java.lang.Math.sqrt
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SpeakerVideoView(viewModel: ZoomViewModel, modifier: Modifier = Modifier) {
    val isJoined = viewModel.isJoined.observeAsState()
    val users = viewModel.users.observeAsState()
    val speakerUsersCount = viewModel.speakerUsersCount.observeAsState()

    Box(modifier = modifier) {
        BoxWithConstraints {
            // グリッドの桁数と行数
            var count = 0
            var col = 1
            var row = 1
            if (isJoined.value == true) {
                count = speakerUsersCount.value!!
            } else {
                // レイアウト確認用
                count = 9
            }
            if (count > 0) {
                col = ceil(sqrt(count.toDouble())).toInt() // 少数部切り上げ
                row = ceil(count.toDouble() / col.toDouble()).toInt() // 少数部切り上げ
            }
            val gridWidth = maxWidth / col
            val gridHeight = maxHeight / row
            val padding = 2.dp

            LazyVerticalGrid(
                cells = GridCells.Fixed(col),
//                contentPadding = PaddingValues(
//                    start = 12.dp,
//                    top = 16.dp,
//                    end = 12.dp,
//                    bottom = 16.dp
//                ),
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center
            ) {
                if (isJoined.value == true && speakerUsersCount.value!! >= 0) {
                    items(users.value!!) { user ->
                        if (!user.isMuted.value!!) {
                            SpeakerVideoItemView(
                                user,
                                modifier = Modifier
                                    .width(gridWidth - padding)
                                    .height(gridHeight - padding)
                                    .padding(padding)
                            )
                        }
                    }
                } else {
                    // レイアウト確認用
                    items(9) { item ->
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .width(gridWidth - padding)
                                .height(gridHeight - padding)
                                .padding(padding)
                                .background(Color.LightGray)
                        ) {
                            SpeakerVideoItemViewDebug(item.toString())
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTime::class)
@Composable
fun SpeakerVideoItemView(
    user: UserViewModel,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .background(Color.LightGray)
        ) {
            ZoomVideoView(
                user.zoomUser.videoCanvas,
                modifier = Modifier
            )
        }

        Text(
            text = user.getName(),
            color = Color.White,
            style = MaterialTheme.typography.caption,
            modifier = Modifier
                .align(alignment = Alignment.BottomStart)
                .padding(4.dp)
                .background(Color.Gray)
                .padding(2.dp)
        )
    }
}

@Composable
fun SpeakerVideoItemViewDebug(
    id: String,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .background(Color.LightGray)
        ) {
            Text(
                text = id,
                style = MaterialTheme.typography.body2,
            )
        }

        Text(
            text = "speaker$id",
            color = Color.White,
            style = MaterialTheme.typography.caption,
            modifier = Modifier
                .align(alignment = Alignment.BottomStart)
                .padding(4.dp)
                .padding(2.dp)

        )
    }
}

@Preview(showBackground = true)
@Composable
fun SpeakerVideoViewPreview() {
    ZoomVideoComposeTheme {
        val viewModel = ZoomViewModel()
        SpeakerVideoView(
            viewModel,
            modifier = Modifier.fillMaxSize()
        )
    }
}