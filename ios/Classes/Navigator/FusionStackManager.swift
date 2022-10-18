//
//  FusionStackManager.swift
//  fusion
//
//  Created by gtbluesky on 2022/3/10.
//

import Foundation
import UIKit

internal class FusionStackManager {
    var pageStack = [WeakReference<FusionViewController>]()
    private var childPageStack = [WeakReference<FusionViewController>]()
    static let instance = FusionStackManager()

    private init() {
    }

    func add(_ container: FusionViewController) {
        pageStack.append(WeakReference(container))
    }

    func remove(_ container: FusionViewController) {
        pageStack.removeAll {
            $0.value == container || $0.value == nil
        }
    }

    func getTopContainer() -> UIViewController? {
        let vc = UIApplication.roofViewController
        let nc = vc?.navigationController
        if nc?.viewControllers.count ?? 0 > 0 {
            return nc?.topViewController
        } else {
            return vc
        }
    }

    func topIsFusionContainer() -> Bool {
        getTopContainer() is FusionViewController
    }

    func closeTopContainer() {
        let vc = UIApplication.roofViewController
        let nc = vc?.navigationController
        if let count = nc?.viewControllers.count {
            if count > 1 {
                nc?.popViewController(animated: true)
            }
        } else {
            if let vc = vc as? FusionViewController {
                vc.dismiss(animated: vc.isViewOpaque)
            } else {
                vc?.dismiss(animated: true)
            }
        }
    }

    func addChild(_ container: FusionViewController) {
        childPageStack.append(WeakReference(container))
    }

    func removeChild(_ container: FusionViewController) {
        childPageStack.removeAll {
            $0.value == container || $0.value == nil
        }
    }

    func notifyEnterForeground() {
        Fusion.instance.engineBinding?.notifyEnterForeground()
        childPageStack.forEach {
            if let vc = $0.value {
                vc.engineBinding?.notifyEnterForeground()
            }
        }
    }

    func notifyEnterBackground() {
        Fusion.instance.engineBinding?.notifyEnterBackground()
        childPageStack.forEach {
            if let vc = $0.value {
                vc.engineBinding?.notifyEnterBackground()
            }
        }
    }

    func sendMessage(msgName: String, msgBody: Dictionary<String, Any>?) {
        var msg: Dictionary<String, Any?> = ["msgName": msgName]
        msg["msgBody"] = msgBody
        Fusion.instance.engineBinding?.onReceive(msg)
        childPageStack.forEach {
            $0.value?.engineBinding?.onReceive(msg)
        }
        UIApplication.roofNavigationController?.viewControllers.forEach {
            ($0 as? PageNotificationListener)?.onReceive(msgName: msgName, msgBody: msgBody)
        }
    }
}