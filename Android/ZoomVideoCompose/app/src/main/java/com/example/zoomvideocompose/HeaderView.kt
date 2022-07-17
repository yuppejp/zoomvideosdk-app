package com.example.zoomvideocompose

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.zoomvideocompose.ui.theme.ZoomVideoComposeTheme

// Material Icons
// https://fonts.google.com/icons?icon.set=Material+Icons

@Composable
fun HeaderView(viewModel: ZoomViewModel, modifier: Modifier = Modifier) {
    val sessionName = viewModel.sessionName.observeAsState()
    val isJoined = viewModel.isJoined.observeAsState()
    val mySelf = viewModel.mySelf.observeAsState()
    val isMuted = mySelf.value?.isMuted?.observeAsState()
    val isVideoOn = mySelf.value?.isVideoOn?.observeAsState()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.padding(8.dp)
    ) {
        Text(text = sessionName.value ?: "")

        Spacer(modifier = Modifier.weight(1.0f))

        if (isVideoOn?.value == true) {
            IconButton(onClick = {
                viewModel.stopVideo()
            }) {
                Icon(Icons.Filled.Videocam, contentDescription = "Videocam")
            }
        } else {
            IconButton(onClick = {
                viewModel.startVideo()
            }) {
                Icon(Icons.Filled.VideocamOff, contentDescription = "Videocam　Off")
            }
        }

        if (isMuted?.value == true) {
            IconButton(onClick = {
                mySelf.value?.unMute()
            }) {
                Icon(Icons.Filled.MicOff, contentDescription = "Mic Off")
            }
        } else {
            IconButton(onClick = {
                mySelf.value?.mute()
            }) {
                Icon(Icons.Filled.Mic, contentDescription = "Mic")
            }
        }

        if (isJoined.value == true) {
            Button(
                onClick = {
                    viewModel.leave()
                },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color.Red,
                    Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.CallEnd,
                    contentDescription = "Call End"
                )
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text("退出")
            }
        } else {
            Button(
                onClick = {
                    viewModel.join()
                }
            ) {
                Icon(
                    imageVector = Icons.Filled.Call,
                    contentDescription = "Call"
                )
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text("参加")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HeaderViewPreview() {
    ZoomVideoComposeTheme {
        val viewModel = ZoomViewModel()
        HeaderView(viewModel)
    }
}