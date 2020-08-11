import UIKit
import Flutter

@UIApplicationMain
@objc class AppDelegate: FlutterAppDelegate {
  override func application(
    _ application: UIApplication,
    didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
  ) -> Bool {
   let flutterViewController: FlutterViewController = window?.rootViewController as! FlutterViewController
     GeneratedPluginRegistrant.register(with: self)
    //https://stackoverflow.com/questions/45645866/how-can-i-push-a-uiviewcontroller-from-flutterviewcontroller
     let navigationController = UINavigationController(rootViewController: flutterViewController)
     navigationController.isNavigationBarHidden = true
     window?.rootViewController = navigationController
     window?.makeKeyAndVisible()
     return super.application(application,     didFinishLaunchingWithOptions: launchOptions)
  }
}
