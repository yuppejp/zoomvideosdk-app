//
//  GroupVideoView.swift
//  ZoomVideoSwiftUI
//

import SwiftUI

struct SpeakerVideoView: View {
    @EnvironmentObject var zoom: ZoomModel
    @State var talking = false
    @State var users: [PreviewUser] = [] // レイアウト確認用
    
    var body: some View {
        GeometryReader { geometry in
            let frameWidth = geometry.frame(in: .global).width
            let frameHeight = geometry.frame(in: .global).height
            
            let xPadding: CGFloat = 4.0
            let yPadding: CGFloat = 4.0
            let (gredRow, gredCol) = getGreadSize()
            let videoWidth = frameWidth / CGFloat(gredCol) - (xPadding * 1)
            let videoHeight = frameHeight / CGFloat(gredRow) - (yPadding * 1)
            
            let columns: [GridItem] = Array(
                repeating: GridItem(.fixed(videoWidth), spacing: xPadding, alignment: .top),
                count: gredCol)
            LazyVGrid(columns: columns, spacing: yPadding) {
                if zoom.isJoined {
                    ForEach(zoom.users.filter({$0.isMuted == false})) { user in
                        SpeakerVideoItemView(user: user, videoWidth: videoWidth, videoHeight: videoHeight)
                            .background(.bar)
                    }
                } else {
                    // レイアウト確認
                    ForEach(users) { user in
                        ZStack {
                            Text("\(user.id)")
                                .font(.caption)
                                .frame(width: videoWidth, height: videoHeight)
                                .border(Color.green, width: talking ? 8.0 : 0)
                                .animation(Animation.easeInOut, value: talking)

                            VStack {
                                Text("\(user.name)")
                                    .font(.caption)
                                    .padding(3)
                                    .foregroundColor(.white)
                                    .background(Color.black.opacity(0.3))
                                    .padding(8)
                            }
                            .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .bottomLeading)
                        }
                        .background(.gray)
                    }
                }
            }
            .padding(2)
            .background(.bar)
        }
        .onAppear {
            // レイアウト確認用
            for i in 1..<10 {
                let user = PreviewUser(id: i, name: "speaker\(i)")
                users.append(user)
            }
            
//            // アニメーションのテスト
//            Timer.scheduledTimer(withTimeInterval: 0.3,
//                                 repeats: true,
//                                 block: { (time: Timer) in
//                talking.toggle()
//            })
        }
    }
    
    private func getGreadSize() -> (row: Int, col: Int) {
        var count = 0
        var row = 0
        var col = 0
        
        if zoom.isJoined {
            // ミュート解除しているユーザー数
            count = zoom.users.filter({$0.isMuted == false}).count
        } else {
            // レイアウト確認用
            count = users.count
        }
        
        // 必要な桁数と行数
        if count > 0 {
            col = Int(ceil(sqrt(Double(count)))) // 少数部切り上げ
            row = Int(ceil(Double(count) / Double(col))) // 少数部切り上げ
        }
        
//        print("getGreadSize")
//        print("  count: \(count)")
//        print("  col: \(col)")
//        print("  row: \(row)")
        return (row, col)
    }
    
}

struct SpeakerVideoItemView: View {
    @ObservedObject var user: User
    @State var animation = false
    var videoWidth: CGFloat
    var videoHeight: CGFloat
    
    var body: some View {
        ZStack {
            if let canvas = user.zoomUser.getVideoCanvas(), let on = canvas.videoStatus()?.on, on == true {
                ZoomVideoView(videoCanvas: user.zoomUser.getVideoCanvas()!)
                    .frame(width: videoWidth, height: videoHeight)
                    .border(Color.green, width: user.isTalking ? 8.0 : 0)
                    .animation(Animation.easeInOut, value: user.isTalking)
            } else {
                Text(Image(systemName: "video.slash.fill"))
                    .font(.title)
                    .frame(width: videoWidth, height: videoHeight)
                    .border(Color.green, width: user.isTalking ? 8.0 : 0)
                    .animation(Animation.easeInOut, value: user.isTalking)
            }
            
            VStack {
                Text(user.getName())
                    .font(.caption)
                    .padding(3)
                    .foregroundColor(.white)
                    .background(Color.black.opacity(0.3))
                    .padding(8)
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .bottomLeading)
        }
    }
}

// レイアウト確認用
struct PreviewUser: Identifiable {
    var id: Int
    var name: String
}

struct GridVideoView_Previews: PreviewProvider {
    static var previews: some View {
        let zoom = ZoomModel()
        SpeakerVideoView()
            .environmentObject(zoom)
            //.preferredColorScheme(.dark)
    }
}
