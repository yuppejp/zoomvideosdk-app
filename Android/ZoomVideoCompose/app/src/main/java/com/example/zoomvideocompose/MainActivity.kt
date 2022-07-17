package com.example.zoomvideocompose

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.zoomvideocompose.ui.theme.ZoomVideoComposeTheme

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<ZoomViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!hasPermissions(this)) {
            requestPermissions(this)
        } else {
            viewModel.initZoomSDK(this)
        }

        setContent {
            ZoomVideoComposeTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    MainView(viewModel)
                }
            }
        }
    }

    private fun hasPermissions(context: Context) =
        permissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }

    private fun requestPermissions(activity: MainActivity) {
        ActivityCompat.requestPermissions(activity, permissions, PERMISSIONS_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            var grantedCount = 0
            for (result in grantResults) {
                if (result == PackageManager.PERMISSION_GRANTED) {
                    grantedCount++
                }
            }
            if (grantResults.size == grantedCount) {
                //　ここに成功時の処理を書く
                viewModel.initZoomSDK(this)
            } else {
                Toast.makeText(applicationContext, "アクセスを許可してください", Toast.LENGTH_LONG)
                    .show()
            }
        }
    }

    companion object {
        const val PERMISSIONS_REQUEST_CODE = 1

        val permissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )
    }
}

@Composable
fun MainView(viewModel: ZoomViewModel, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        HeaderView(viewModel)

        Row(modifier = Modifier.weight(0.7f)) {
            LocalVideoView(
                viewModel,
                modifier = Modifier
                    .padding(start = 4.dp, end = 4.dp)
                    .aspectRatio(1f)
            )

            MutedVideoView(viewModel)
        }

        SpeakerVideoView(
            viewModel,
            modifier = Modifier
                .weight(4f)
        )

        ChatView(
            viewModel,
            modifier = Modifier
                .fillMaxSize()
                .weight(3f)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ZoomVideoComposeTheme {
        val viewModel = ZoomViewModel()
        MainView(viewModel)
    }
}