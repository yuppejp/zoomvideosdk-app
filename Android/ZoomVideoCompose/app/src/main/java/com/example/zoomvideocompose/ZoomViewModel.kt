package com.example.zoomvideocompose

import android.annotation.SuppressLint
import android.app.Activity
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import us.zoom.sdk.*
import java.text.SimpleDateFormat
import java.util.*

class UserViewModel(user: ZoomVideoSDKUser) : ViewModel() {
    var isMuted = MutableLiveData(true)
    var isVideoOn = MutableLiveData(true)
    var isTalking = MutableLiveData(false)
    var zoomUser: ZoomVideoSDKUser

    init {
        zoomUser = user
    }

    fun getName(): String {
        return zoomUser.userName
    }

    fun mute() {
        ZoomVideoSDK.getInstance().audioHelper?.muteAudio(zoomUser)
    }

    fun unMute() {
        val mySelf = ZoomVideoSDK.getInstance().session.mySelf
        if (zoomUser == mySelf) {
            // 自分自身の場合、オーディオが開始していない場合は開始する
            ZoomVideoSDK.getInstance().audioHelper?.startAudio()
        }

        ZoomVideoSDK.getInstance().audioHelper?.unMuteAudio(zoomUser)
    }
}

class ChatMessage(userName: String, text: String) {
    var userName: String
    var text: String
    var date: String

    init {
        this.userName = userName
        this.text = text
        this.date = SimpleDateFormat("HH:mm").format(Date())
    }
}

class ZoomViewModel : ViewModel() {
    var isJoined = MutableLiveData(false)
    var sessionName = MutableLiveData("(未参加)")
    var mySelf = MutableLiveData<UserViewModel>()
    val users = MutableLiveData<ArrayList<UserViewModel>>()
    val mutedUsersCount = MutableLiveData(0)
    val speakerUsersCount = MutableLiveData(0)
    val messages = MutableLiveData<ArrayList<ChatMessage>>()
    val messageCount = MutableLiveData(0)
    private val myName = makeRandomString(6) // ランダムな名前を生成
    private val tag = "MyDebug#ZoomModel"

    init {
        users.value = ArrayList<UserViewModel>() // 空のリスト
        messages.value = ArrayList<ChatMessage>() // 空のリスト
    }

    fun addMessage(userName: String, text: String) {
        val msg = ChatMessage(userName, text)
        messages.value?.add(msg)

        // 変更通知
        messageCount.value = messages.value?.size
    }

    fun sendMessage(text: String) {
        val chatHelper = ZoomVideoSDK.getInstance().chatHelper
        chatHelper?.sendChatToAll(text)
    }

    fun join() {
        val audioOptions = ZoomVideoSDKAudioOption().apply {
            connect = true // Auto connect to audio upon joining
            mute = true // Auto mute audio upon joining
        }
        val videoOptions = ZoomVideoSDKVideoOption().apply {
            localVideoOn = true // Turn on local/self video upon joining
        }
        val params = ZoomVideoSDKSessionContext().apply {
            sessionName = "Session1"
            userName = myName
            sessionPassword = "123"
            token = "" // "Your jwt"
            audioOption = audioOptions
            videoOption = videoOptions
        }

        val session = ZoomVideoSDK.getInstance().joinSession(params)
        if (session != null) {
            Log.d(tag, "joinSession")
            Log.d(tag, "  sessionName: $session.sessionName")
            Log.d(tag, "  sessionID: $session.sessionID")

            val videoHelper = ZoomVideoSDK.getInstance().videoHelper
            videoHelper.startVideo()
        }
    }

    fun leave() {
        val shouldEndSession = false
        ZoomVideoSDK.getInstance().leaveSession(shouldEndSession)
    }

    fun startVideo() {
        val videoHelper = ZoomVideoSDK.getInstance().videoHelper
        videoHelper?.startVideo()
    }

    fun stopVideo() {
        val videoHelper = ZoomVideoSDK.getInstance().videoHelper
        videoHelper?.stopVideo()
    }

    fun updateUsersCount() {
        var muters = 0
        var speakers = 0
        for (user in users.value!!) {
            if (user.isMuted.value!!) {
                muters += 1
            } else {
                speakers += 1
            }
        }
        mutedUsersCount.postValue(muters)
        speakerUsersCount.postValue(speakers)
    }

    fun initZoomSDK(activity: Activity) {
        val params = ZoomVideoSDKInitParams().apply {
            domain = "https://zoom.us" // Required
            logFilePrefix = "MyLogPrefix" // Optional for debugging
            enableLog = true // Optional for debugging
        }
        val sdk = ZoomVideoSDK.getInstance()
        val initResult = sdk.initialize(activity, params)
        if (initResult == ZoomVideoSDKErrors.Errors_Success) {
            // You have successfully initialized the SDK
            Log.d(tag, "initialize: successfully")

        } else {
            // Something went wrong, see error code documentation
            Log.d(tag, "initialize: failed")
        }

        val listener = object : ZoomVideoSDKDelegate {
            override fun onSessionJoin() {
                Log.d(tag, "onSessionJoin")

                isJoined.postValue(true)

                val session = ZoomVideoSDK.getInstance().session
                val user = UserViewModel(session.mySelf)
                sessionName.postValue(session.sessionName)
                mySelf.postValue(user)
            }

            override fun onSessionLeave() {
                Log.d(tag, "onSessionLeave")

                isJoined.postValue(false)
                sessionName.postValue("")
                users.value?.clear()
            }

            override fun onError(errorCode: Int) {
                when (errorCode) {
                    ZoomVideoSDKErrors.Errors_Session_Join_Failed -> { // 2003
                        // トークンの期限切れかも
                        Log.d(tag, "onError: Errors_Session_Join_Failed($errorCode)")
                    }

                    else -> {
                        Log.d(tag, "onError: $errorCode")
                    }
                }
            }

            @SuppressLint("WrongViewCast")
            override fun onUserJoin(
                userHelper: ZoomVideoSDKUserHelper?,
                userList: MutableList<ZoomVideoSDKUser>?
            ) {
                Log.d(tag, "onUserJoin")

//                // UI更新用に詰め直す
//                val newUsers = ArrayList<UserViewModel>()
//                if (users.value != null) {
//                    newUsers.addAll(users.value!!)
//                }

                userList?.forEach { user ->
                    Log.d(tag, "  userName: ${user.userName}")

                    val userModel = UserViewModel(user)
                    users.value?.add(userModel)
                }

//                users.value = newUsers
                updateUsersCount()
            }

            override fun onUserLeave(
                userHelper: ZoomVideoSDKUserHelper?,
                userList: MutableList<ZoomVideoSDKUser>?
            ) {
                Log.d(tag, "onUserLeave")

//                // UI更新用に詰め直す
//                val newUsers = ArrayList<UserViewModel>()
//                if (users.value != null) {
//                    newUsers.addAll(users.value!!)
//                }

                userList?.forEach { user ->
                    Log.d(tag, "  id: " + user.userID)
                    Log.d(tag, "  name: " + user.userName)

                    for (userModel in users.value!!) {
                        if (userModel.zoomUser == user) {
                            users.value?.remove(userModel)
                        }
                    }
                }

//                users.postValue(newUsers)
                updateUsersCount()
            }

            override fun onUserVideoStatusChanged(
                videoHelper: ZoomVideoSDKVideoHelper?,
                userList: MutableList<ZoomVideoSDKUser>?
            ) {
                Log.d(tag, "onUserVideoStatusChanged")

                if (userList != null) {
                    for (zoomUser in userList) {
                        val isOn = zoomUser.videoCanvas.videoStatus.isOn

                        for (user in users.value!!) {
                            if (user.zoomUser == zoomUser) {
                                user.isVideoOn.value = isOn
                            }
                        }

                        if (mySelf.value?.zoomUser == zoomUser) {
                            mySelf.value?.isVideoOn?.value = isOn
                            //mySelf.value?.isVideoOn?.postValue(isOn)
                        }
                        Log.d(tag, "  isOn: $isOn")
                    }
                }
            }

            override fun onUserAudioStatusChanged(
                audioHelper: ZoomVideoSDKAudioHelper?,
                userList: MutableList<ZoomVideoSDKUser>?
            ) {
                Log.d(tag, "onUserAudioStatusChanged")

                if (userList != null) {
                    for (zoomUser in userList) {
                        val isMuted = zoomUser.audioStatus.isMuted
                        Log.d(tag, "  isMuted: $isMuted")

                        for (user in users.value!!) {
                            if (user.zoomUser == zoomUser) {
                                user.isMuted.value = isMuted
                            }
                        }

                        if (mySelf.value?.zoomUser == zoomUser) {
                            mySelf.value?.isMuted?.postValue(isMuted)
                        }
                    }
                }

                updateUsersCount()
            }

            override fun onUserShareStatusChanged(
                shareHelper: ZoomVideoSDKShareHelper?,
                userInfo: ZoomVideoSDKUser?,
                status: ZoomVideoSDKShareStatus?
            ) {
                Log.d(tag, "onUserShareStatusChanged")
            }

            override fun onLiveStreamStatusChanged(
                liveStreamHelper: ZoomVideoSDKLiveStreamHelper?,
                status: ZoomVideoSDKLiveStreamStatus?
            ) {
                Log.d(tag, "onLiveStreamStatusChanged")
            }

            override fun onChatNewMessageNotify(
                chatHelper: ZoomVideoSDKChatHelper?,
                messageItem: ZoomVideoSDKChatMessage?
            ) {
                Log.d(tag, "onChatNewMessageNotify")

                if (messageItem != null) {
                    addMessage(
                        messageItem.senderUser.userName,
                        messageItem.content)
                }
            }

            override fun onUserHostChanged(
                userHelper: ZoomVideoSDKUserHelper?,
                userInfo: ZoomVideoSDKUser?
            ) {
                Log.d(tag, "onUserHostChanged")
            }

            override fun onUserManagerChanged(user: ZoomVideoSDKUser?) {
                Log.d(tag, "onUserManagerChanged")
            }

            override fun onUserNameChanged(user: ZoomVideoSDKUser?) {
                Log.d(tag, "onUserNameChanged")
            }

            override fun onUserActiveAudioChanged(
                audioHelper: ZoomVideoSDKAudioHelper?,
                list: MutableList<ZoomVideoSDKUser>?
            ) {
                Log.d(tag, "onUserActiveAudioChanged")

                if (list != null) {
                    for (zoomUser in list) {
                        val userName = zoomUser.userName
                        val isTalking = zoomUser.audioStatus.isTalking
                        Log.d(tag, "  user: ${userName}, isTalking: $isTalking")

                        for (user in users.value!!) {
                            if (user.zoomUser == zoomUser) {
                                user.isTalking.value = true
                            }
                        }

                        if (mySelf.value?.zoomUser == zoomUser) {
                            mySelf.value?.isTalking?.postValue(isTalking)
                        }
                    }
                }
            }

            override fun onSessionNeedPassword(handler: ZoomVideoSDKPasswordHandler?) {
                Log.d(tag, "onSessionNeedPassword")
            }

            override fun onSessionPasswordWrong(handler: ZoomVideoSDKPasswordHandler?) {
                Log.d(tag, "onSessionPasswordWrong")
            }

            override fun onMixedAudioRawDataReceived(rawData: ZoomVideoSDKAudioRawData?) {
                Log.d(tag, "onMixedAudioRawDataReceived")
            }

            override fun onOneWayAudioRawDataReceived(
                rawData: ZoomVideoSDKAudioRawData?,
                user: ZoomVideoSDKUser?
            ) {
                Log.d(tag, "onOneWayAudioRawDataReceived")
            }

            override fun onShareAudioRawDataReceived(rawData: ZoomVideoSDKAudioRawData?) {
                Log.d(tag, "onShareAudioRawDataReceived")
            }

            override fun onCommandReceived(sender: ZoomVideoSDKUser?, strCmd: String?) {
                Log.d(tag, "onCommandReceived")
            }

            override fun onCommandChannelConnectResult(isSuccess: Boolean) {
                Log.d(tag, "onCommandChannelConnectResult")
            }

            override fun onCloudRecordingStatus(status: ZoomVideoSDKRecordingStatus?) {
                Log.d(tag, "onCloudRecordingStatus")
            }

            override fun onHostAskUnmute() {
                Log.d(tag, "onHostAskUnmute")
            }

            override fun onInviteByPhoneStatus(
                status: ZoomVideoSDKPhoneStatus?,
                reason: ZoomVideoSDKPhoneFailedReason?
            ) {
                Log.d(tag, "onInviteByPhoneStatus")
            }

            override fun onMultiCameraStreamStatusChanged(
                status: ZoomVideoSDKMultiCameraStreamStatus?,
                user: ZoomVideoSDKUser?,
                videoPipe: ZoomVideoSDKRawDataPipe?
            ) {
                Log.d(tag, "onMultiCameraStreamStatusChanged")
            }

            override fun onMultiCameraStreamStatusChanged(
                status: ZoomVideoSDKMultiCameraStreamStatus?,
                user: ZoomVideoSDKUser?,
                canvas: ZoomVideoSDKVideoCanvas?
            ) {
                Log.d(tag, "onMultiCameraStreamStatusChanged")
            }
        }

        ZoomVideoSDK.getInstance().addListener(listener)
    }

    private fun makeRandomString(length: Int): String {
        val charset = "ABCDEFGHIJKLMNOPQRSTUVWXTZabcdefghiklmnopqrstuvwxyz0123456789"
        return (1..length)
            .map { charset.random() }
            .joinToString("")
    }
}