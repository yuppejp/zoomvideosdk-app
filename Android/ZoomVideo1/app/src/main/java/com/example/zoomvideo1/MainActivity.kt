package com.example.zoomvideo1

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import us.zoom.sdk.*

class MainActivity : AppCompatActivity() {
    private val TAG = "MyDebug#MainActivity"
    private lateinit var joinButton: Button
    private lateinit var localVideoView: ZoomVideoSDKVideoView
    private lateinit var remoteVideoView: ZoomVideoSDKVideoView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        joinButton = findViewById(R.id.joinButton)
        localVideoView = findViewById(R.id.localVideoView)
        remoteVideoView = findViewById(R.id.remoteVideoView)

        if (!hasPermissions(this)) {
            requestPermissions(this)
        } else {
            initZoomSDK()
        }

        joinButton.setOnClickListener {
            if (ZoomVideoSDK.getInstance().isInSession) {
                leave()
            } else {
                val userNameEditText = findViewById<EditText>(R.id.userNameEditText)
                join(userNameEditText.text.toString())
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
                initZoomSDK()
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

    private fun join(name: String) {
        val audioOptions = ZoomVideoSDKAudioOption().apply {
            connect = true // Auto connect to audio upon joining
            mute = true // Auto mute audio upon joining
        }
        val videoOptions = ZoomVideoSDKVideoOption().apply {
            localVideoOn = true // Turn on local/self video upon joining
        }
        val params = ZoomVideoSDKSessionContext().apply {
            sessionName = "Session1"
            userName = name
            sessionPassword = "123"
            token = "" // Your jwt
            audioOption = audioOptions
            videoOption = videoOptions
        }

        val session = ZoomVideoSDK.getInstance().joinSession(params)
        if (session != null) {
            Log.d(TAG, "joinSession")
            Log.d(TAG, "  sessionName: $session.sessionName")
            Log.d(TAG, "  sessionID: $session.sessionID")

            val videoHelper = ZoomVideoSDK.getInstance().videoHelper
            videoHelper.startVideo()
            //videoHelper.switchCamera()
        }
    }

    private fun leave() {
        val shouldEndSession = false
        ZoomVideoSDK.getInstance().leaveSession(shouldEndSession)
    }

    private fun initZoomSDK() {
        val params = ZoomVideoSDKInitParams().apply {
            domain = "https://zoom.us" // Required
            logFilePrefix = "MyLogPrefix" // Optional for debugging
            enableLog = true // Optional for debugging
        }
        val sdk = ZoomVideoSDK.getInstance()
        val initResult = sdk.initialize(this, params)
        if (initResult == ZoomVideoSDKErrors.Errors_Success) {
            // You have successfully initialized the SDK
            Log.d(TAG, "initialize: successfully")

        } else {
            // Something went wrong, see error code documentation
            Log.d(TAG, "initialize: failed")
        }

        val listener = object : ZoomVideoSDKDelegate {
            override fun onSessionJoin() {
                Log.d(TAG, "onSessionJoin")

                val session = ZoomVideoSDK.getInstance().session
                session.mySelf.videoCanvas.subscribe(
                    localVideoView,
                    ZoomVideoSDKVideoAspect.ZoomVideoSDKVideoAspect_Original
                )

                joinButton.setText("Leave")
            }

            override fun onSessionLeave() {
                Log.d(TAG, "onSessionLeave")

                val session = ZoomVideoSDK.getInstance().session
                session.mySelf.videoCanvas.unSubscribe(localVideoView) // todo: unSubscribeしてもViewがクリアされない

                leave()
                joinButton.setText("Join")
            }

            override fun onError(errorCode: Int) {
                when (errorCode) {
                    ZoomVideoSDKErrors.Errors_Session_Join_Failed -> { // 2003
                        // トークンの期限切れかも
                        Log.d(TAG, "onError: Errors_Session_Join_Failed($errorCode)")
                    }

                    else -> {
                        Log.d(TAG, "onError: $errorCode")
                    }
                }
            }

            @SuppressLint("WrongViewCast")
            override fun onUserJoin(
                userHelper: ZoomVideoSDKUserHelper?,
                userList: MutableList<ZoomVideoSDKUser>?
            ) {
                Log.d(TAG, "onUserJoin")
                userList?.forEach { user ->
                    Log.d(TAG, "  userName: ${user.userName}")

                    user.videoCanvas.subscribe(
                        remoteVideoView,
                        ZoomVideoSDKVideoAspect.ZoomVideoSDKVideoAspect_Original
                    )
                }
            }

            override fun onUserLeave(
                userHelper: ZoomVideoSDKUserHelper?,
                userList: MutableList<ZoomVideoSDKUser>?
            ) {
                Log.d(TAG, "onUserLeave")

                userList?.forEach { user ->
                    Log.d(TAG, "  id: " + user.userID)
                    Log.d(TAG, "  name: " + user.userName)

                    user.videoCanvas.unSubscribe(remoteVideoView) // todo: unSubscribeしてもViewがクリアされない
                }
            }

            override fun onUserVideoStatusChanged(
                videoHelper: ZoomVideoSDKVideoHelper?,
                userList: MutableList<ZoomVideoSDKUser>?
            ) {
                Log.d(TAG, "onUserVideoStatusChanged")
            }

            override fun onUserAudioStatusChanged(
                audioHelper: ZoomVideoSDKAudioHelper?,
                userList: MutableList<ZoomVideoSDKUser>?
            ) {
                Log.d(TAG, "onUserAudioStatusChanged")
            }

            override fun onUserShareStatusChanged(
                shareHelper: ZoomVideoSDKShareHelper?,
                userInfo: ZoomVideoSDKUser?,
                status: ZoomVideoSDKShareStatus?
            ) {
                Log.d(TAG, "onUserShareStatusChanged")
            }

            override fun onLiveStreamStatusChanged(
                liveStreamHelper: ZoomVideoSDKLiveStreamHelper?,
                status: ZoomVideoSDKLiveStreamStatus?
            ) {
                Log.d(TAG, "onLiveStreamStatusChanged")
            }

            override fun onChatNewMessageNotify(
                chatHelper: ZoomVideoSDKChatHelper?,
                messageItem: ZoomVideoSDKChatMessage?
            ) {
                Log.d(TAG, "onChatNewMessageNotify")
            }

            override fun onUserHostChanged(
                userHelper: ZoomVideoSDKUserHelper?,
                userInfo: ZoomVideoSDKUser?
            ) {
                Log.d(TAG, "onUserHostChanged")
            }

            override fun onUserManagerChanged(user: ZoomVideoSDKUser?) {
                Log.d(TAG, "onUserManagerChanged")
            }

            override fun onUserNameChanged(user: ZoomVideoSDKUser?) {
                Log.d(TAG, "onUserNameChanged")
            }

            override fun onUserActiveAudioChanged(
                audioHelper: ZoomVideoSDKAudioHelper?,
                list: MutableList<ZoomVideoSDKUser>?
            ) {
                Log.d(TAG, "onUserActiveAudioChanged")
            }

            override fun onSessionNeedPassword(handler: ZoomVideoSDKPasswordHandler?) {
                Log.d(TAG, "onSessionNeedPassword")
            }

            override fun onSessionPasswordWrong(handler: ZoomVideoSDKPasswordHandler?) {
                Log.d(TAG, "onSessionPasswordWrong")
            }

            override fun onMixedAudioRawDataReceived(rawData: ZoomVideoSDKAudioRawData?) {
                Log.d(TAG, "onMixedAudioRawDataReceived")
            }

            override fun onOneWayAudioRawDataReceived(
                rawData: ZoomVideoSDKAudioRawData?,
                user: ZoomVideoSDKUser?
            ) {
                Log.d(TAG, "onOneWayAudioRawDataReceived")
            }

            override fun onShareAudioRawDataReceived(rawData: ZoomVideoSDKAudioRawData?) {
                Log.d(TAG, "onShareAudioRawDataReceived")
            }

            override fun onCommandReceived(sender: ZoomVideoSDKUser?, strCmd: String?) {
                Log.d(TAG, "onCommandReceived")
            }

            override fun onCommandChannelConnectResult(isSuccess: Boolean) {
                Log.d(TAG, "onCommandChannelConnectResult")
            }

            override fun onCloudRecordingStatus(status: ZoomVideoSDKRecordingStatus?) {
                Log.d(TAG, "onCloudRecordingStatus")
            }

            override fun onHostAskUnmute() {
                Log.d(TAG, "onHostAskUnmute")
            }

            override fun onInviteByPhoneStatus(
                status: ZoomVideoSDKPhoneStatus?,
                reason: ZoomVideoSDKPhoneFailedReason?
            ) {
                Log.d(TAG, "onInviteByPhoneStatus")
            }

            override fun onMultiCameraStreamStatusChanged(
                status: ZoomVideoSDKMultiCameraStreamStatus?,
                user: ZoomVideoSDKUser?,
                videoPipe: ZoomVideoSDKRawDataPipe?
            ) {
                Log.d(TAG, "onMultiCameraStreamStatusChanged")
            }

            override fun onMultiCameraStreamStatusChanged(
                status: ZoomVideoSDKMultiCameraStreamStatus?,
                user: ZoomVideoSDKUser?,
                canvas: ZoomVideoSDKVideoCanvas?
            ) {
                Log.d(TAG, "onMultiCameraStreamStatusChanged")
            }
        }

        ZoomVideoSDK.getInstance().addListener(listener)
    }
}