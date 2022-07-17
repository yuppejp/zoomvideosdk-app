package com.example.zoomvideocompose

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import us.zoom.sdk.ZoomVideoSDKVideoAspect
import us.zoom.sdk.ZoomVideoSDKVideoCanvas
import us.zoom.sdk.ZoomVideoSDKVideoView

@Composable
fun ZoomVideoView(canvas: ZoomVideoSDKVideoCanvas, modifier: Modifier = Modifier) {
    AndroidView(
        factory = ::ZoomVideoSDKVideoView,
        update = { zoomVideoSDKVideoView ->
            canvas.subscribe(
                zoomVideoSDKVideoView,
                ZoomVideoSDKVideoAspect.ZoomVideoSDKVideoAspect_PanAndScan
            )
        },
        modifier = modifier
    )
}