package com.example.zoomvideocompose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.zoomvideocompose.ui.theme.ZoomVideoComposeTheme


@Composable
fun BalloonText(
    text: String,
    modifier: Modifier = Modifier,
    balloonColor: Color = Color(red = 109, green = 230, blue = 123),
    mirrored: Boolean = false
) {
    if (mirrored) {
        YourBalloonText(text, modifier, balloonColor)
    } else {
        MyBalloonText(text, modifier, balloonColor)
    }
}

@Composable
fun MyBalloonText(
    text: String,
    modifier: Modifier = Modifier,
    balloonColor: Color = Color.Green
) {
    val cornerRadius = with(LocalDensity.current) { 8.dp.toPx() }
    val tailSize = Size(cornerRadius / 2, cornerRadius / 2)
    val tailWidthDp = with(LocalDensity.current) { tailSize.width.toDp() }

    val balloonShape = GenericShape { size, _ ->
        val shapeRect = Rect(Offset(0f, 0f), Size(size.width, size.height))
        val arcSize = Size(cornerRadius * 2, cornerRadius * 2)
        var arcRect: Rect
        var x: Float
        var y: Float
        var controlX: Float
        var controlY: Float

        // 時計回りに描いていく

        // 左上角丸
        x = shapeRect.left
        y = shapeRect.top
        arcRect = Rect(Offset(x, y), arcSize)
        arcTo(
            rect = arcRect,
            startAngleDegrees = 180f,
            sweepAngleDegrees = 90f,
            forceMoveTo = false
        )

        // 右上角丸
        x = shapeRect.right - (cornerRadius * 2) - tailSize.width
        y = shapeRect.top
        arcRect = Rect(Offset(x, y), arcSize)
        arcTo(arcRect, 270f, 45f, false)

        // しっぽ上部
        x = shapeRect.right
        y = shapeRect.top
        controlX = shapeRect.right - tailSize.width / 2
        controlY = shapeRect.top
        //lineTo(x, y)
        quadraticBezierTo(controlX, controlY, x, y)

        // しっぽ下部
        x = shapeRect.right - tailSize.width
        y = shapeRect.top + (cornerRadius / 2) + tailSize.height
        controlX = shapeRect.right - tailSize.width / 2
        controlY = shapeRect.top
        //lineTo(x, y)
        quadraticBezierTo(controlX, controlY, x, y)

        // 右下角丸
        x = shapeRect.right - tailSize.width - cornerRadius * 2
        y = shapeRect.bottom - (cornerRadius * 2)
        arcRect = Rect(Offset(x, y), arcSize)
        arcTo(arcRect, 0f, 90f, false)

        // 左下角丸
        x = shapeRect.left
        y = shapeRect.bottom - (cornerRadius * 2)
        arcRect = Rect(Offset(x, y), arcSize)
        arcTo(arcRect, 90f, 90f, false)
    }

    Text(
        text,
        modifier = modifier
            .background(balloonColor, shape = balloonShape)
            .padding(start = 4.dp, end = tailWidthDp + 2.dp)
            .padding(vertical = 1.dp)
    )
}

@Composable
fun YourBalloonText(
    text: String,
    modifier: Modifier = Modifier,
    balloonColor: Color = Color.White
) {
    val cornerRadius = with(LocalDensity.current) { 8.dp.toPx() }
    val tailSize = Size(cornerRadius / 2, cornerRadius / 2)
    val tailWidthDp = with(LocalDensity.current) { tailSize.width.toDp() }

    val balloonShape = GenericShape { size, _ ->
        val shapeRect = Rect(Offset(0f, 0f), Size(size.width, size.height))
        val arcSize = Size(cornerRadius * 2, cornerRadius * 2)
        var arcRect: Rect
        var x: Float
        var y: Float
        var controlX: Float
        var controlY: Float

        // 反時計回りに描画していく

        // 左上角丸
        x = shapeRect.left + tailSize.width
        y = shapeRect.top
        arcRect = Rect(Offset(x, y), arcSize)
        arcTo(arcRect, 270f, -45f, false)

        // しっぽ上部
        x = shapeRect.left
        y = shapeRect.top
        controlX = shapeRect.left + tailSize.width / 2
        controlY = shapeRect.top
        //lineTo(x, y)
        quadraticBezierTo(controlX, controlY, x, y)

        // しっぽ下部
        x = shapeRect.left + tailSize.width
        y = shapeRect.top + (cornerRadius / 2) + tailSize.height
        controlX = shapeRect.left + tailSize.width / 2
        controlY = shapeRect.top
        //lineTo(x, y)
        quadraticBezierTo(controlX, controlY, x, y)

        // 左下角丸
        x = shapeRect.left + tailSize.width
        y = shapeRect.bottom - (cornerRadius * 2)
        arcRect = Rect(Offset(x, y), arcSize)
        arcTo(arcRect, 180f, -90f, false)

        // 右下角丸
        x = shapeRect.right - cornerRadius
        y = shapeRect.bottom - cornerRadius * 2
        arcRect = Rect(Offset(x - cornerRadius, y), arcSize)
        arcTo(arcRect, 90f, -90f, false)

        // 右上角丸
        x = shapeRect.right - cornerRadius * 2
        y = shapeRect.top
        arcRect = Rect(Offset(x, y), arcSize)
        arcTo(arcRect, 0f, -90f, false)
    }

    Text(
        text,
        modifier = modifier
            .background(balloonColor, shape = balloonShape)
            .padding(start = tailWidthDp + 4.dp, end = 2.dp)
            .padding(vertical = 1.dp)
    )
}


@Preview(showBackground = true)
@Composable
fun BalloonTextPreview() {
    ZoomVideoComposeTheme {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                "Android表示サンプル",
                modifier = Modifier.padding(4.dp)
            )
            BalloonText(
                "Composeから\nこんにちは!",
                modifier = Modifier.padding(4.dp)
            )
            BalloonText(
                "逆向きの吹き出しです", mirrored = true,
                modifier = Modifier.padding(4.dp)
            )
            BalloonText(
                "長いテキストの表示例です。テキストの幅と高さに合わせて、吹き出しの大きさも自動的に連動して表示されます。",
                modifier = Modifier.padding(4.dp)
            )
        }
    }
}