//
//  ZoomVideo.swift
//  ZoomVideoSwiftUI
//

import SwiftUI
import ZoomVideoSDK

struct ZoomVideoView: UIViewControllerRepresentable {
    let videoCanvas: ZoomVideoSDKVideoCanvas

    func makeUIViewController(context: Context) -> UIViewController {
        let viewController = UIViewController()
        viewController.view.backgroundColor = .lightGray
        
        return viewController
    }
    
    func updateUIViewController(_ viewController: UIViewController, context: Context) {
        let videoAspect = ZoomVideoSDKVideoAspect.panAndScan
        videoCanvas.subscribe(with: viewController.view, andAspectMode: videoAspect)
    }
    
    final class Coordinator: NSObject {
        let parent: ZoomVideoView
        init(_ parent: ZoomVideoView) {
            self.parent = parent
        }
    }

    func makeCoordinator() -> Coordinator {
        Coordinator(self)
    }

    static func dismantleUIView(_ viewController: UIViewController, coordinator: Coordinator) {
        coordinator.parent.videoCanvas.unSubscribe(with: viewController.view)
    }
}
