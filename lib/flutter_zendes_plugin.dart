import 'dart:async';

import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';
import 'package:package_info/package_info.dart';

class FlutterZendeskPlugin {
  static const MethodChannel _channel =
      const MethodChannel('flutter_zendes_plugin');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    debugPrint('version = "$version"');
    return version;
  }

  Future<void> init(String accountKey,
      {String applicationId,
      String clientId,
      String domainUrl,
      String nameIdentifier,
      String emailIdentifier,
      String phone,
      String name,
      String email,
      String departmentName}) async {
    if (applicationId == null || applicationId.isEmpty) {
      PackageInfo pi = await PackageInfo.fromPlatform();
      applicationId = '${pi.appName}, v${pi.version}(${pi.buildNumber})';
    }
    debugPrint('Init with applicationId="$applicationId"');
    final String result = await _channel.invokeMethod('init', <String, dynamic>{
      'accountKey': accountKey,
      'applicationId': applicationId,
      'clientId': clientId,
      'domainUrl': domainUrl,
      'emailIdentifier': emailIdentifier,
      'nameIdentifier': nameIdentifier,
      'phone': phone,
      'email': email,
      'name': name,
      'departmentName': departmentName,
    });
    debugPrint('Init result ="$result"');
  }

  Future<void> startChatV2(
      {String phone,
      String name,
      String email,
      String botLabel,
      String toolbarTitle,
      bool endChatSwitch,
      String departmentName}) async {
    return await _channel.invokeMethod('startChatV2', <String, dynamic>{
      'phone': phone,
      'email': email,
      'name': name,
      'botLabel': botLabel,
      'toolbarTitle': toolbarTitle,
      'departmentName': departmentName,
      'endChatSwitch': endChatSwitch,
    });
  }

  ///
  /// Zendesk Helpcenter call, variables differ from android to iOS.
  ///
  /// Android variables:
  /// - categoriesCollapsed
  /// - contactUsButtonVisible
  /// - showConversationsMenuButton
  ///
  /// iOS variables:
  /// - categoryShowContactOptions
  /// - categoryShowContactOptionsOnEmptySearch
  /// - articleShowContactOptions
  Future<dynamic> helpCenter({
      bool categoriesCollapsed,
      bool contactUsButtonVisible,
      bool showConversationsMenuButton,
      bool categoryShowContactOptions,
      bool categoryShowContactOptionsOnEmptySearch,
      bool articleShowContactOptions,
  }) async {
    return await _channel.invokeMethod('helpCenter', <String, dynamic>{
      'categoriesCollapsed': categoriesCollapsed,
      'contactUsButtonVisible': contactUsButtonVisible,
      'showConversationsMenuButton': showConversationsMenuButton,
      'categoryShowContactOptions': categoryShowContactOptions,
      'categoryShowContactOptionsOnEmptySearch': categoryShowContactOptionsOnEmptySearch,
      'articleShowContactOptions': articleShowContactOptions,
    });
  }

  Future<dynamic> requestViewAction() async {
    return await _channel.invokeMethod('requestView');
  }

  Future<dynamic> requestListViewAction() async {
    return await _channel.invokeMethod('requestListView');
  }

  Future<dynamic> changeNavStatusAction(bool isShow) async {
    return await _channel
        .invokeMethod('changeNavStatus', <String, dynamic>{'isShow': isShow});
  }
}
