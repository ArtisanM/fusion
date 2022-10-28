//
//  HostViewController.swift
//  Runner
//
//  Created by gtbluesky on 2022/3/10.
//

import Foundation
import UIKit
import fusion

class HostViewController: UIViewController {
//class HostViewController: UIViewController, UIViewControllerRestoration {

    @IBOutlet weak var button0: UIButton!
    @IBOutlet weak var button1: UIButton!
    @IBOutlet weak var button2: UIButton!
    @IBOutlet weak var button3: UIButton!

    override func viewDidLoad() {
        super.viewDidLoad()
//        self.restorationClass = HostViewController.self
//        self.restorationIdentifier = NSStringFromClass(HostViewController.self)
        FusionNotificationBinding.instance.register(self)
    }

    deinit {
        FusionNotificationBinding.instance.unregister(self)
    }

    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        self.navigationController?.setNavigationBarHidden(true, animated: animated)
    }

//    class func viewController(withRestorationIdentifierPath identifierComponents: [String], coder: NSCoder) -> UIViewController? {
//        let storyboard = UIStoryboard(name: "Main", bundle: nil)
//        let vc = storyboard.instantiateViewController(withIdentifier: "HostVC")
//        vc.restorationClass = self
//        vc.restorationIdentifier = identifierComponents.last
//        return vc
//    }

    @IBAction func click0(_ sender: UIButton) {
        FusionNavigator.instance.open("/lifecycle", arguments: ["title": "iOS Flutter Page"])
    }

    @IBAction func click1(_ sender: Any) {
        FusionNavigator.instance.open("/transparent", arguments: ["title": "iOS Flutter Page", "transparent": true])
    }

    @IBAction func click2(_ sender: Any) {
        navigationController?.pushViewController(MultiViewController(), animated: true)
    }

    @IBAction func click3(_ sender: Any) {
        let fusionVc = CustomViewController(isReused: false, routeName: "/lifecycle", routeArguments: nil)
        presentLeftDrawer(fusionVc, animated: true)
    }
}

extension HostViewController: FusionNotificationListener {
    public func onReceive(name: String, body: Dictionary<String, Any>?) {
        NSLog("onReceive: name=\(name), body=\(body)")
    }
}
