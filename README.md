# flutter_zendesk_chat_support_plugin

A Zendesk Chat&Support Flutter plugin.

## Getting Started

```yaml
dependencies:
  flutter_zendes_plugin: ^1.0.2
```

## Import in your Widget

```dart
import 'package:flutter_zendes_plugin/flutter_zendes_plugin.dart';
```

## Implementation for Android

Add activities to project manifest, if you want to change themes, just add it to styles and change it on its respective activity

```xml
<activity
    android:name="zendesk.messaging.MessagingActivity"
    android:theme="@style/Theme.AppCompat" />
<activity
    android:name="zendesk.support.guide.HelpCenterActivity"
    android:theme="@style/Theme.AppCompat" />
<activity
    android:name="zendesk.support.requestlist.RequestListActivity"
    android:theme="@style/Theme.AppCompat" />
<activity
    android:name="zendesk.support.request.RequestActivity"
     android:theme="@style/Theme.AppCompat" />
<activity
    android:name="zendesk.support.guide.ViewArticleActivity"
     android:theme="@style/Theme.AppCompat" />
```

## Implementation for iOS

In you projectRoot/ios/Runner/AppDelegate.swift

```swift
import UIKit
import Flutter

@UIApplicationMain
@objc class AppDelegate: FlutterAppDelegate {
  override func application(
    _ application: UIApplication,
    didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
  ) -> Bool {
     GeneratedPluginRegistrant.register(with: self)
     //https://stackoverflow.com/questions/45645866/how-can-i-push-a-uiviewcontroller-from-flutterviewcontroller
     let flutterViewController: FlutterViewController = window?.rootViewController as! FlutterViewController
     let navigationController = UINavigationController(rootViewController: flutterViewController)
     navigationController.isNavigationBarHidden = true
     window?.rootViewController = navigationController
     window?.makeKeyAndVisible()
     return super.application(application,     didFinishLaunchingWithOptions: launchOptions)
  }
}
```
