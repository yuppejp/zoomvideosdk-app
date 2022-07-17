//
//  MyVideoView.swift
//  ZoomVideoSwiftUI
//

import SwiftUI

struct LocalVideoView: View {
    @EnvironmentObject var zoom: ZoomModel

    var body: some View {
        VStack {
            if zoom.isJoined, let canvas = zoom.mySelf.zoomUser.getVideoCanvas() {
                ZStack {
                    ZoomVideoView(videoCanvas: canvas)
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                    
                    VStack(spacing: 0) {
                        Text(zoom.userName)
                            .font(.caption)
                            .padding(2)
                            .foregroundColor(.white)
                            .background(Color.black.opacity(0.3))
                            .padding(4)
                    }
                    .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .bottomLeading)
                }
            } else {
                // レイアウト確認用
                ZStack {
                    Text("My Video")
                        .font(.caption2)
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                        .background(.gray)
                    
                    VStack(spacing: 0) {
                        Text("myself")
                            .font(.caption2)
                            .padding(2)
                            .foregroundColor(.white)
                            .background(Color.black.opacity(0.3))
                            .padding(4)
                    }
                    .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .bottomLeading)
                }
            }
        }
    }
}

struct LocalVideoView_Previews: PreviewProvider {
    static var previews: some View {
        let zoom = ZoomModel()
        LocalVideoView()
            .environmentObject(zoom)
    }
}
