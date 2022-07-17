//
//  ContentView.swift
//  ZoomVideoSwiftUI
//

import SwiftUI
import ZoomVideoSDK

struct ContentView: View {
    var body: some View {
        let zoom = ZoomModel()
        MainView()
            .environmentObject(zoom)
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        let zoom = ZoomModel()
        ContentView()
            .environmentObject(zoom)
            .previewInterfaceOrientation(.portrait)
            //.preferredColorScheme(.dark)
    }
}

struct MainView: View {
    @EnvironmentObject var zoom: ZoomModel

    var body: some View {
        VStack(spacing: 0) {
            GeometryReader { geometry in
                let width = geometry.frame(in: .global).width
                let height = geometry.frame(in: .global).height

                if (width < height) {
                    // ポートレート
                    VStack(spacing: 0) {
                        HeaderView(mySelf: zoom.mySelf)
                            //.frame(height: height * 0.1)

                        HStack(spacing: 0) {
                            LocalVideoView()
                                .aspectRatio(1/1, contentMode: .fit)
                                .padding(4)
                            MutedVideoView()
                        }
                        .frame(height: height * 0.1)

                        SpeakerVideoView()
                            .frame(maxHeight: .infinity)

                        ChatView()
                            .frame(height: height * 0.35)
                    }
                } else {
                    // ランドスケープ
                    VStack(spacing: 0) {
                        HeaderView(mySelf: zoom.mySelf)
                            //.frame(height: height * 0.15)

                        HStack(spacing: 0) {
                            VStack(spacing: 0) {
                                HStack(spacing: 0) {
                                    LocalVideoView()
                                        .aspectRatio(1/1, contentMode: .fit)
                                        .padding(2)
                                    
                                    MutedVideoView()
                                }
                                .frame(height: height * 0.2)

                                SpeakerVideoView()
                                    .frame(maxHeight: .infinity)
                            }
                            .frame(width: width * 0.7)
                            
                            ChatView()
                                .frame(maxHeight: .infinity)
                                .padding(.leading, 4)
                        }
                    }
                }
            }
        }
    }
}

