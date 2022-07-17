//
//  ChatView.swift
//  ZoomVideoSwiftUI
//

import SwiftUI

struct ChatView: View {
    @EnvironmentObject var zoom: ZoomModel
    @State var inputText = ""
    
    var body: some View {
        VStack(spacing: 0) {
            ScrollViewReader { reader in
                List {
                    ForEach(zoom.messages) { message in
                        MessageItemView(message: message, myUserName: zoom.userName)
                            .font(.footnote)
                            .background(.bar)
                    }
                    .listRowSeparator(.hidden) // 境界線を非表示
                    .listRowInsets(EdgeInsets(top:0, leading: 0, bottom:0, trailing: 0)) // 行余白を詰める
                }
                .listStyle(PlainListStyle())
                .onChange(of: zoom.lastMessageId) { id in
                    // 末尾へスクロール
                    withAnimation(.linear(duration: 2)) {
                        reader.scrollTo(id)
                    }
                }
            }
            .onTapGesture {
                // キーボードを閉じる
                UIApplication.shared.closeKeyboard()
            }

            HStack {
                TextField("メッセージを入力...", text: $inputText, onCommit: {
                    if !inputText.isEmpty {
                        zoom.sendMessage(text: inputText)
                        inputText = "" // クリア
                    }
                })
                .font(.footnote)
                .keyboardType(.default)
                .textFieldStyle(RoundedBorderTextFieldStyle())
                .padding(4)
            }
            .background(.background)
        }
        .background(.bar)
        .onAppear {
            // レイアウト確認用
            if !zoom.isJoined {
                for i in 1..<4 {
                    zoom.addMessage(userName: "user" + String(i), text: "メッセージ" + String(i))
                }
                zoom.addMessage(userName: zoom.userName, text: "返信メッセージ1")
            }
        }
    }
}

struct MessageItemView: View {
    var message: Message
    var myUserName: String
    
    var body: some View {
        if message.userName == myUserName {
            // 自分のメッセージ
            MyMessageItemView(message: message)
        } else {
            // 相手のメッセージ
            YourMessageItemView(message: message)
        }
    }
}

struct YourMessageItemView: View {
    var message: Message
    
    var body: some View {
        HStack(spacing: 0) {
            VStack(alignment: .leading, spacing: 0) {
                Text(message.userName)
                    .font(.caption2)
                    .padding(.leading, 4)
                
                BalloonText(message.text, mirrored: true)
                    //.font(.body)
                    .padding(.leading, 8)
            }
            
            VStack() {
                Text(message.date.formatTime())
                    .font(.caption2)
                    .padding(.leading, 4)
                    .frame(maxHeight: .infinity, alignment: .bottom)
            }
            
            Spacer()
                .frame(maxWidth: .infinity)
        }
    }
}

struct MyMessageItemView: View {
    @State var message: Message
    
    var body: some View {
        HStack(spacing: 0) {
            Spacer()
                .frame(maxWidth: .infinity)
            
            VStack() {
                Text(message.date.formatTime())
                    .font(.caption2)
                    .padding(.leading, 4)
                    .frame(maxHeight: .infinity, alignment: .bottom)
            }
            
            VStack(alignment: .trailing, spacing: 0) {
                Text(message.userName)
                    .font(.caption2)
                    .padding(.trailing, 4)
                
                BalloonText(message.text, mirrored: false)
                    //.font(.body)
                    .padding(.trailing, 8)
            }
        }
    }
}


extension Date {
    func formatTime() -> String {
        let f = DateFormatter()
        f.timeStyle = .short
        f.dateStyle = .none
        f.locale = Locale(identifier: "ja_JP")
        let time = f.string(from: self)
        return time
    }
}

extension UIApplication {
    func closeKeyboard() {
        sendAction(#selector(UIResponder.resignFirstResponder), to: nil, from: nil, for: nil)
    }
}

struct ChatView_Previews: PreviewProvider {
    static var previews: some View {
        let zoom = ZoomModel()
        ChatView()
            .environmentObject(zoom)
            .previewInterfaceOrientation(.portraitUpsideDown)
    }
}
