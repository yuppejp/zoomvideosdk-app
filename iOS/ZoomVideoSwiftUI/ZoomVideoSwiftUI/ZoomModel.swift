//
//  ZoomModel.swift
//  ZoomVideoSwiftUI
//

import SwiftUI
import ZoomVideoSDK

class User: Identifiable, ObservableObject {
    @Published var isMuted = true
    @Published var isVideoOn = false
    @Published var isTalking  = false
    let id = UUID()
    var zoomUser: ZoomVideoSDKUser
    
    init(zoomUser: ZoomVideoSDKUser) {
        self.zoomUser = zoomUser
    }
    
    func getName() -> String {
        if let name = zoomUser.getName() {
            return name
        } else {
            return ""
        }
    }

    func mute() {
        if let audioHelper = ZoomVideoSDK.shareInstance()?.getAudioHelper() {
            audioHelper.muteAudio(zoomUser)
        }
    }

    func unmute() {
        if let session = ZoomVideoSDK.shareInstance()?.getSession(), let myself = session.getMySelf() {
            // 自分自身の場合、オーディオが開始していない場合は開始する
            if myself == zoomUser {
                if let audioStatus = myself.audioStatus() {
                    if audioStatus.audioType == .none {
                        if let audioHelper = ZoomVideoSDK.shareInstance()?.getAudioHelper() {
                            audioHelper.startAudio()
                        }
                    }
                }
            }
        }

        if let audioHelper = ZoomVideoSDK.shareInstance()?.getAudioHelper() {
            audioHelper.unmuteAudio(zoomUser)
        }
    }
    
//    func isMuted(_ zoomVideoSDKUser: ZoomVideoSDKUser? = nil) -> Bool {
//        var user = zoomVideoSDKUser
//        if user == nil {
//            user = localUser
//        }
//        var isMuted = true
//        if isJoined, let audioStatus = user?.audioStatus() {
//            if audioStatus.audioType != .none {
//                isMuted = audioStatus.isMuted
//            }
//        }
//        return isMuted
//    }
//
//    func isVideoOn() -> Bool {
//        var isVideoOn = false
//        if let canvas = zoomUser?.getVideoCanvas(), let on = canvas.videoStatus()?.on {
//            isVideoOn = on
//        }
//        return isVideoOn
//    }
    
}

struct Message: Identifiable {
    var id = UUID()
    var userName: String
    var date = Date()
    var text: String
}

class ZoomModel: NSObject, ZoomVideoSDKDelegate, ObservableObject {
    @Published var sessionName = "(未参加)"
    @Published var isJoined = false
    @Published var users: [User] = []
    @Published var mySelf = User(zoomUser: ZoomVideoSDKUser())
    @Published var messages: [Message] = []
    @Published var lastMessageId = UUID()
    var initialized = false
    var userName = generator(6) // ランダムな名前を生成
    
    func addMessage(userName: String, text: String) {
        let msg = Message(userName: userName, text: text)
        messages.append(msg)
        lastMessageId = msg.id
    }

    func sendMessage(text: String) {
        sendChatToAll(text)
    }
    
    func zoomInit() {
        ZoomVideoSDK.shareInstance()?.delegate = self
        
        let initParams = ZoomVideoSDKInitParams()
        initParams.domain = "zoom.us"
        initParams.enableLog = true
        
        let sdkInitReturnStatus = ZoomVideoSDK.shareInstance()?.initialize(initParams)
        switch sdkInitReturnStatus {
        case .Errors_Success:
            print("SDK initialized successfully")
            initialized = true
        default:
            if let error = sdkInitReturnStatus {
                print("SDK failed to initialize: \(error)")
            }
        }
    }
    
    func join() {
        if (!initialized) {
            zoomInit()
        }
        
        let sessionContext = ZoomVideoSDKSessionContext()
        sessionContext.token = "" // Your jwt
        sessionContext.sessionName = "Session1" // "Your session name"
        sessionContext.sessionPassword = "123"  // "Your session password"
        sessionContext.userName = userName
        
        let videoOption = ZoomVideoSDKVideoOptions()
        videoOption.localVideoOn = true
        sessionContext.videoOption = videoOption
        
        let audioOption = ZoomVideoSDKAudioOptions()
        audioOption.connect = true
        audioOption.mute = true
        sessionContext.audioOption = audioOption
        
        if let session = ZoomVideoSDK.shareInstance()?.joinSession(sessionContext) {
            print("joinSession: successfully")
//            print("  id: \(String(describing: session.getID()))")
//            print("  host: \(String(describing: session.getHost()))")
            print("  name: \(String(describing: session.getName()))")
//            print("  host: \(String(describing: session.getHost()))")
//            print("  pawword: \(String(describing: session.getPassword()))")
//            print("  mySelf: \(String(describing: session.getMySelf()))")
//            print("  remoteUser: \(String(describing: session.getRemoteUsers()))")
//            print("  host: \(String(describing: session.getHost()))")
            sessionName = session.getName()!
        } else {
            print("joinSession: failed")
        }
    }
    
    func leave() {
        // end: if end the session for host. YES if the host should end the entire session, or NO if the host should just leave the session.
        var shouldEndSession = false
        if mySelf.zoomUser.isHost() {
            // 自分がホストの場合はセッションを強制終了（とりあえず）
            shouldEndSession = true
        }
        ZoomVideoSDK.shareInstance()?.leaveSession(shouldEndSession)
    }
    
    func startVideo() {
        if let videoHelper = ZoomVideoSDK.shareInstance()?.getVideoHelper() {
            videoHelper.startVideo()
        }
    }

    func stopVideo() {
        if let videoHelper = ZoomVideoSDK.shareInstance()?.getVideoHelper() {
            videoHelper.stopVideo()
        }
    }
    
    func sendChatToAll(_ text: String) {
        if let chatHelper = ZoomVideoSDK.shareInstance()?.getChatHelper() {
            chatHelper.sendChat(toAll: text)
        }
    }

    // see: https://qiita.com/Kosuke-214/items/649ed2454aee695c2e00
    static func generator(_ length: Int) -> String {
        let letters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        var randomString = ""
        for _ in 0 ..< length {
            randomString += String(letters.randomElement()!)
        }
        return randomString
    }

    func onError(_ ErrorType: ZoomVideoSDKError, detail details: Int) {
        switch ErrorType {
        case .Errors_Success:
            print("Success")
        case .Errors_Session_Service_Invaild:
            print("Errors_Session_Service_Invaild \(ErrorType) \(details)")
        case .Errors_Session_Join_Failed:
            print("Errors_Session_Join_Failed \(ErrorType) \(details)")
        case .Errors_Session_Disconncting:
            print("Errors_Session_Disconncting \(ErrorType) \(details)")
        case .Errors_Session_Invalid_Param:
            print("Errors_Session_Invalid_Param \(ErrorType) \(details)")
        default:
            print("Error \(ErrorType) \(details)")
        }
    }

    func onSessionJoin() {
        print("onSessionJoin")

        if let session = ZoomVideoSDK.shareInstance()?.getSession() {
            if let user = session.getMySelf() {
                print("  id: \(String(describing: user.getID()))")
                print("  name: \(String(describing: user.getName()))")

                mySelf.zoomUser = user
            }
        }
        isJoined = true
        messages.removeAll()
    }
    
    func onSessionLeave() {
        print("onSessionLeave")

        isJoined = false
        users.removeAll()
    }
    
    func onUserJoin(_ helper: ZoomVideoSDKUserHelper?, users userArray: [ZoomVideoSDKUser]?) {
        print("onUserJoin")
        
        if let userArray = userArray {
            for user in userArray {
                print("  id: \(user.getID())")
                print("  name: \(String(describing: user.getName()))")
                
                // 一覧に追加
                let user = User(zoomUser: user)
                users.append(user)
            }
        }
    }
    
    func onUserLeave(_ helper: ZoomVideoSDKUserHelper?, users userArray: [ZoomVideoSDKUser]?) {
        print("onUserLeave")
        
        if let userArray = userArray {
            for user in userArray {
                print(user)
                
                // 一覧から削除
                for (i, item) in users.enumerated() {
                    if item.zoomUser == user {
                        users.remove(at: i)
                        break
                    }
                }
            }
        }
    }
    
    func onUserVideoStatusChanged(_ helper: ZoomVideoSDKVideoHelper?, user userArray: [ZoomVideoSDKUser]?) {
        print("onUserVideoStatusChanged")

        if let userArray = userArray {
            for user in userArray {
                print("  id: \(user.getID())")
                print("  name: \(String(describing: user.getName()))")

                var isVideoOn = false
                if let canvas = user.getVideoCanvas(), let on = canvas.videoStatus()?.on {
                    isVideoOn = on
                }
                print("  on: \(isVideoOn)")

                for item in users {
                    if item.zoomUser == user {
                        item.isVideoOn = isVideoOn
                        break
                    }
                }
                if user == mySelf.zoomUser {
                    mySelf.isVideoOn = isVideoOn
                }
            }
        }
    }

    func onUserAudioStatusChanged(_ helper: ZoomVideoSDKAudioHelper?, user userArray: [ZoomVideoSDKUser]?) {
        print("onUserAudioStatusChanged")
        
        if let userArray = userArray {
            for user in userArray {
                if let audioType = user.audioStatus()?.audioType {
                    print("  audioType: \(audioType)")
                }

                if let isMuted = user.audioStatus()?.isMuted  {
                    print("  isMuted: \(isMuted)")
                    
                    for item in users {
                        if item.zoomUser == user {
                            item.isMuted = isMuted
                            self.objectWillChange.send() // 再描画
                            break
                        }
                    }
                    if user == mySelf.zoomUser {
                        mySelf.isMuted = isMuted
                    }
                }
            }
        }
    }
    
    func onLiveStreamStatusChanged(_ helper: ZoomVideoSDKLiveStreamHelper?, status: ZoomVideoSDKLiveStreamStatus) {
        print("onLiveStreamStatusChanged")

        switch status {
        case .inProgress:
            print("Live stream now in progress.")
        case .ended:
            print("Live stream has ended.")
        default:
            print("Live stream status unknown.")
        }
    }
    
    func onChatNewMessageNotify(_ helper: ZoomVideoSDKChatHelper?, message: ZoomVideoSDKChatMessage?) {
        print("onChatNewMessageNotify")

        if let content = message?.content, let senderName = message?.senderUser?.getName() {
            print("\(senderName) sent a message: \(content)")
            
            addMessage(userName: senderName, text: content)
        }
    }
    
    func onUserHostChanged(_ helper: ZoomVideoSDKUserHelper?, users user: ZoomVideoSDKUser?) {
        print("onUserHostChanged")

        if let userName = user!.getName() {
            print("\(userName): is the new host.")
        }
    }
    
    func onUserActiveAudioChanged(_ helper: ZoomVideoSDKUserHelper?, users userArray: [ZoomVideoSDKUser]?) {
        print("onUserActiveAudioChanged")

        if let userArray = userArray {
            for user in userArray {
                // Use .audioStatus.talking to see if the user is currently talking.
                if let audioStatus = user.audioStatus() {
                    let isTalking = audioStatus.talking
                    
                    if let userName = user.getName() {
                        print("[onUserActiveAudioChanged]\(userName) isTalking:\(isTalking)")
                    }
                    
                    for item in users {
                        if item.zoomUser == user {
                            item.isTalking = isTalking

                            let timer = Timer.scheduledTimer(withTimeInterval: 0.3,
                                                             repeats: true,
                                                             block: { (time: Timer) in
                                item.isTalking.toggle()
                                print("interval_func")
                            })
                            
                            Timer.scheduledTimer(withTimeInterval: 3,
                                                 repeats: false,
                                                 block: { (time: Timer) in
                                timer.invalidate()
                                item.isTalking = false
                                print("timer: stop")
                            })
                            
                            break
                        }
                    }
                    if user == mySelf.zoomUser {
                        mySelf.isTalking = isTalking
                    }

                }
            }
        }
    }
    
    func onSessionNeedPassword(_ completion: ((String?, Bool) -> ZoomVideoSDKError)?) {
        print("onSessionNeedPassword")

        // Recommended action: prompt user to enter password again and pass user input into completion
        if let completion = completion {
            let userInput = "password"
            let cancelJoinSession = false
            let completionReturnValue = completion(userInput, cancelJoinSession)
            print("completionReturnValue: \(completionReturnValue)")
        }
        // Alternatively, you may abandon the attempt to join the session
        // let userInput = nil
        // let cancelJoinSession = true
        // let completionReturnValue = completion(userInput, cancelJoinSession)
        // print("Completion return value code: (completionReturnValue)")
    }
    
    func onSessionPasswordWrong(_ completion: ((String?, Bool) -> ZoomVideoSDKError)?) {
        print("onSessionPasswordWrong")

        // Recommended action: prompt user to enter password again and pass user input into completion
        if let completion = completion {
            let userInput = "password"
            let cancelJoinSession = false
            let completionReturnValue = completion(userInput, cancelJoinSession)
            print("completionReturnValue: \(completionReturnValue)")
        }
        // Alternatively, you may abandon the attempt to join the session
        // let userInput = nil
        // let cancelJoinSession = true
        // let completionReturnValue = completion(userInput, cancelJoinSession)
        // print("Completion return value code: (completionReturnValue)")
    }
}

