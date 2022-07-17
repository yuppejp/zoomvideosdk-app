package com.example.zoomvideocompose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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

@Composable
fun LocalVideoView(viewModel: ZoomViewModel, modifier: Modifier = Modifier) {
    val mySelf = viewModel.mySelf.observeAsState()

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier.fillMaxSize()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.LightGray)
            ) {
                mySelf.value?.zoomUser?.videoCanvas?.let { videoCanvas ->
                    ZoomVideoView(videoCanvas)
                } ?: run {
                    // レイアウト確認用
                    Text(
                        text = "My Video",
                        style = MaterialTheme.typography.caption
                    )
                }
            }

            Text(
                text = mySelf.value?.getName() ?: "myself",
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
}

@Preview(showBackground = true)
@Composable
fun LocalVideoViewPreview() {
    ZoomVideoComposeTheme {
        val viewModel = ZoomViewModel()
        LocalVideoView(viewModel)
    }
}