//
//  ViewController.swift
//  ZoomVideoSwift1
//

import UIKit
import ZoomVideoSDK

class ViewController: UIViewController, ZoomVideoSDKDelegate {

    @IBOutlet weak var userNameTextField: UITextField!
    @IBOutlet weak var remoteVideoView: UIView!
    @IBOutlet weak var localViewView: UIView!
    @IBOutlet weak var joinButton: UIButton!

    private var userName = "user123"
    private var myUserID: UInt = 0
    private var isJoined = false

    @IBAction func joinTouchUp(_ sender: Any) {
        userName = userNameTextField.text!
        print("userName: \(userName)")
        
        if let isInSession = ZoomVideoSDK.shareInstance()?.isInSession() {
            if isInSession {
                leave()
            } else {
                join()
            }
        }
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view.
        
        userNameTextField.text = userName
        
        zoomInit()
    }

    func zoomInit() {
        let initParams = ZoomVideoSDKInitParams()
        initParams.domain = "zoom.us"
        initParams.enableLog = true
        
        let sdkInitReturnStatus = ZoomVideoSDK.shareInstance()?.initialize(initParams)
        switch sdkInitReturnStatus {
            case .Errors_Success:
                print("SDK initialized successfully")
            
                ZoomVideoSDK.shareInstance()?.delegate = self
            
            default:
                if let error = sdkInitReturnStatus {
                    print("SDK failed to initialize: \(error)")
            }
        }
    }
    
    let token = "" // Your jwt
    func join() {
        // Create a SessionContext Object
        let sessionContext = ZoomVideoSDKSessionContext()
        // Ensure that you do not hard code JWT or any other confidential credentials in your production app.
        sessionContext.token = token //"Your jwt"
        sessionContext.sessionName = "Session1" // "Your session name"
        sessionContext.sessionPassword = "123" // "Your session password"
        sessionContext.userName = userName

        let videoOption = ZoomVideoSDKVideoOptions()
        videoOption.localVideoOn = true
        sessionContext.videoOption = videoOption
        
        let audioOption = ZoomVideoSDKAudioOptions()
        audioOption.connect = true
        audioOption.mute = false
        sessionContext.audioOption = audioOption
        
        if let session = ZoomVideoSDK.shareInstance()?.joinSession(sessionContext) {
            print("Session joined successfully.")
//            print("  id: \(String(describing: session.getID()))")
//            print("  host: \(String(describing: session.getHost()))")
            print("  name: \(String(describing: session.getName()))")
//            print("  host: \(String(describing: session.getHost()))")
//            print("  pawword: \(String(describing: session.getPassword()))")
//            print("  mySelf: \(String(describing: session.getMySelf()))")
//            print("  remoteUser: \(String(describing: session.getRemoteUsers()))")
//            print("  host: \(String(describing: session.getHost()))")
        } else {
            print("joinSession: failed.")
        }
    }
    
    func leave() {
        // end: if end the session for host. YES if the host should end the entire session, or NO if the host should just leave the session.
        ZoomVideoSDK.shareInstance()?.leaveSession(false)
        myUserID = 0
    }

    func onError(_ ErrorType: ZoomVideoSDKError, detail details: Int) {
        switch ErrorType {
        case .Errors_Success:
            // Your ZoomVideoSDK operation was successful.
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
            // Your ZoomVideoSDK operation raised an error.
            // Refer to error code documentation.
            print("Error \(ErrorType) \(details)")
            return
        }
    }
    
    func onSessionJoin() {
        // You have successfully joined the session.
        print("onSessionJoin")

        if let session = ZoomVideoSDK.shareInstance()?.getSession() {
            if let user = session.getMySelf() {
                print("  id: \(String(describing: user.getID()))")
                print("  name: \(String(describing: user.getName()))")
                myUserID = user.getID()
            }
        }

        isJoined = true
        joinButton.setTitle("Leave", for: .normal)
    }
    
    func onSessionLeave() {
        // You have successfully left the session.
        print("onSessionLeave")

        isJoined = false
        joinButton.setTitle("Join", for: .normal)
    }
    
    func onUserJoin(_ helper: ZoomVideoSDKUserHelper?, users userArray: [ZoomVideoSDKUser]?) {
        print("onUserJoin")
        
        // UserArray contains the new users.
        // Use helper to perform actions on a user.
        if let userArray = userArray {
            for user in userArray {
                print("  id: \(user.getID())")
                print("  name: \(String(describing: user.getName()))")

//                // 自分のカメラをON
//                if user.getName() == userName {
//                    if let videoHelper = ZoomVideoSDK.shareInstance()?.getVideoHelper() {
//                        videoHelper.startVideo()
//                    }
//                }
            }
        }
    }
    
    func onUserLeave(_ helper: ZoomVideoSDKUserHelper?, users userArray: [ZoomVideoSDKUser]?) {
        print("onUserLeave")
        
        // UserArray contains the users that recently left.
        // Use helper to perform actions on a user.
        if let userArray = userArray {
            for user in userArray {
                print(user)
                if let usersVideoCanvas = user.getVideoCanvas() {
                    if user.getID() == myUserID {
                        usersVideoCanvas.unSubscribe(with: localViewView)
                    } else {
                        usersVideoCanvas.unSubscribe(with: remoteVideoView)
                    }
                }
            }
        }
    }
    
    func onUserVideoStatusChanged(_ helper: ZoomVideoSDKVideoHelper?, user userArray: [ZoomVideoSDKUser]?) {
        print("onUserVideoStatusChanged")

        // UserArray contains the users that had a video status change.
        // Use helper to perform actions on a user.
        if let userArray = userArray {
            for user in userArray {
                print("  id: \(user.getID())")
                print("  name: \(String(describing: user.getName()))")

//                // Use .videoStatus.on to check if a user's video is on or off.
//                if let videoStatus = user.videoStatus() {
//                    print("  videoStatus: \(videoStatus.on)")
//                }

                if let usersVideoCanvas = user.getVideoCanvas() {
                    // Subscribe User's videoCanvas to render their video stream.
                    let videoAspect = ZoomVideoSDKVideoAspect.panAndScan

                    if user.getID() == myUserID {
                        usersVideoCanvas.subscribe(with: localViewView, andAspectMode: videoAspect)
                    } else {
                        usersVideoCanvas.subscribe(with: remoteVideoView, andAspectMode: videoAspect)
                    }

//                    if let videoHelper = ZoomVideoSDK.shareInstance()?.getVideoHelper() {
//                        videoHelper.startVideo()
//                    }
                }

                // 親Viewを更新すると子Viewが裏に隠れるので前面に持ってくる
                remoteVideoView.bringSubviewToFront(localViewView)
            }
        }
    }

    func onUserAudioStatusChanged(_ helper: ZoomVideoSDKAudioHelper?, user userArray: [ZoomVideoSDKUser]?) {
        print("onUserAudioStatusChanged")
        
        if let userArray = userArray {
            for user in userArray {
                if let isMuted = user.audioStatus()?.isMuted  {
                    print("  isMuted: \(isMuted)")
                }
                
                if let audioType = user.audioStatus()?.audioType {
                    print("  audioType: \(audioType)")
                }
            }
        }
    }
    
    func onLiveStreamStatusChanged(_ helper: ZoomVideoSDKLiveStreamHelper?, status: ZoomVideoSDKLiveStreamStatus) {
        print("onLiveStreamStatusChanged")

        // Use helper to perform live stream actions.
        // Status is the new live stream status.
        switch status {
        case .inProgress:
            print("Live stream now in progress.")
        case .ended:
            print("Live stream has ended.")
        default:
            print("Live stream status unknown.")
        }
    }
    
    func onChatNewMessageNotify(_ helper: ZoomVideoSDKChatHelper?, message chatMessage: ZoomVideoSDKChatMessage?) {
        print("onChatNewMessageNotify")

        // Use helper to perform chat actions.
        // Message contains the info about a chat message.
        //if let content = chatMessage.content, let senderName = chatMessage.senderUser.getName() {
        //    print("(senderName) sent a message: (content)")
        //}
    }
    
    func onUserHostChanged(_ helper: ZoomVideoSDKUserHelper?, users user: ZoomVideoSDKUser?) {
        print("onUserHostChanged")

        // User is the new host of the session.
        // Use helper to perform actions on a user.
        if let userName = user!.getName() {
            print("\(userName): is the new host.")
        }
    }
    
    // onUserActiveAudioChanged is different from onUserAudioStatusChanged. onUserActiveAudioChanged is called when a given user's audio changes, while onUserAudioStatusChanged is called when the user's audio status changes. For example, if the user is unmuted and is using their device's microphone, this callback will be triggered whenever their microphone detects a noise.
    func onUserActiveAudioChanged(_ helper: ZoomVideoSDKUserHelper?, users userArray: [ZoomVideoSDKUser]?) {
        print("onUserActiveAudioChanged")

        // UserArray contains the users that had an active audio change.
        // Use helper to perform audio actions.
        if let userArray = userArray {
            for user in userArray {
                // Use .audioStatus.talking to see if the user is currently talking.
                if let audioStatus = user.audioStatus(), audioStatus.talking, let userName = user.getName() {
                    print("\(userName) began talking.")
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
