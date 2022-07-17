//
//  SessionView.swift
//  ZoomVideoSwiftUI
//

import SwiftUI

struct HeaderView: View {
    @EnvironmentObject var zoom: ZoomModel
    @ObservedObject var mySelf: User // View更新監視用
    
    var body: some View {
        HStack(spacing: 0) {
            Text(zoom.sessionName)
                .font(.title2)
            
            Spacer()
            
            Button(action: {
                if mySelf.isVideoOn {
                    zoom.stopVideo()
                } else {
                    zoom.startVideo()
                }
            }, label: {
                Image(systemName: mySelf.isVideoOn ? "video.fill" : "video.slash.fill")
            })
            .tint(.primary)
            
            Button(action: {
                if mySelf.isMuted {
                    mySelf.unmute()
                } else {
                    mySelf.mute()
                }
            }, label: {
                Image(systemName: mySelf.isMuted ? "mic.slash.fill" : "mic.fill")
            })
            .tint(.primary)
            .padding()
            
            Button(action: {
                if zoom.isJoined {
                    zoom.leave()
                } else {
                    zoom.join()
                }
            }) {
                HStack {
                    Image(systemName: "phone.fill")
                    Text(zoom.isJoined ? "退出" : "参加")
                }
            }
            .buttonStyle(.borderedProminent)
            .tint(zoom.isJoined ? .red : .green)
        }
        .padding(.leading, 8)
        .padding(.trailing, 8)
    }
}

struct SessionView_Previews: PreviewProvider {
    static var previews: some View {
        let zoom = ZoomModel()
        VStack {
            HeaderView(mySelf: zoom.mySelf)
                .environmentObject(zoom)
                //.preferredColorScheme(.dark)
            Spacer()
        }
    }
}
