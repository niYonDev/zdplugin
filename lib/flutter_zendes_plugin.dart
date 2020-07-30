import 'dart:async';

import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';
import 'package:package_info/package_info.dart';

class FlutterZendesPlugin {
  static const MethodChannel _channel =
      const MethodChannel('flutter_zendes_plugin');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    debugPrint('version = "$version"');
    return version;
  }

  Future<void> init(String accountKey, {String applicationId}) async {
    if (applicationId == null || applicationId.isEmpty) {
      PackageInfo pi = await PackageInfo.fromPlatform();
      applicationId = '${pi.appName}, v${pi.version}(${pi.buildNumber})';
    }
    debugPrint('Init with applicationId="$applicationId"');
    final String result = await _channel.invokeMethod('init', <String, dynamic>{
      'accountKey': accountKey,
      'applicationId': applicationId,
    });
    debugPrint('Init result ="$result"');
  }

  Future<void> startChat() async {
    return await _channel.invokeMethod('startChat');
  }

  Future<void> helpCenter() async {
    return await _channel.invokeMethod('helpCenter');
  }
}
