//
//  MutedVideoView.swift
//  ZoomVideoSwiftUI
//

import SwiftUI

struct MutedVideoView: View {
    @EnvironmentObject var zoom: ZoomModel
    @State var users: [PreviewUser] = [] // レイアウト確認用
    
    var body: some View {
        GeometryReader { geometry in
            //let viewWidth = geometry.frame(in: .global).width
            let viewHeight = geometry.frame(in: .global).height
            
            let videoHeight = viewHeight * 0.6
            let videoWidth = videoHeight
            
            ScrollView(.horizontal) {
                HStack {
                    if zoom.isJoined {
                        ForEach(zoom.users.filter({$0.isMuted == true})) { user in
                            MutedVideoItemView(user: user, videoWidth: videoWidth, videoHeight: videoHeight)
                        }
                    } else {
                        ForEach(users) { user in
                            VStack(spacing: 0) {
                                // レイアウト確認用
                                Text("\(user.id)")
                                    .font(.caption2)
                                    .frame(width: videoWidth, height: videoHeight)
                                    .background(.gray)
                                    .clipShape(Circle())

                                Text(user.name)
                                    .frame(maxHeight: .infinity)
                                    .font(.caption2)
                            }
                        }
                    }
                }
            }
            .padding(4)
            //.background(Color(red: 44/255, green: 43/255, blue: 44/255, opacity: 1.0))
        }
        .background(.bar)
        .onAppear {
            if !zoom.isJoined {
                // レイアウト確認用
                for i in 1..<6{
                    let user = PreviewUser(id: i, name: "user\(i)")
                    users.append(user)
                }
            }
        }
    }
}

struct MutedVideoItemView: View {
    @ObservedObject var user: User
    var videoWidth: CGFloat
    var videoHeight: CGFloat

    var body: some View {
        
        VStack(spacing: 0) {
            if let canvas = user.zoomUser.getVideoCanvas(), let on = canvas.videoStatus()?.on, on == true {
                ZoomVideoView(videoCanvas: user.zoomUser.getVideoCanvas()!)
                    .frame(width: videoWidth, height: videoHeight)
                    .clipShape(Circle())
            } else {
                Text(Image(systemName: "video.slash.fill"))
                    .frame(width: videoWidth, height: videoHeight)
                    .background(.gray)
                    .clipShape(Circle())
            }
            
            Text(user.zoomUser.getName()!)
                .frame(maxHeight: .infinity)
                .font(.caption2)
        }
    }
}

struct MutedVideoView_Previews: PreviewProvider {
    static var previews: some View {
        let zoom = ZoomModel()
        VStack {
            MutedVideoView()
                .frame(height: 100)
                .environmentObject(zoom)
            Spacer()
        }
    }
}
