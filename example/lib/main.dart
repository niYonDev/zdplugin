import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:flutter_zendes_plugin/flutter_zendesk_plugin.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';
  String _accountKey = 'tFh19KFTd8BBqP3gihF65iF9ep7q4sLa';
  String _applicationId = '6783b5375f399d60da5242b77dc5fa5a888c51d39d841212';
  String _clientId = 'mobile_sdk_client_a93b8a067e553b6f2f1f';
  String _domainUrl = 'https://brplay.zendesk.com';
  FlutterZendeskPlugin _flutterPlugin = FlutterZendeskPlugin();

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    String platformVersion;
    // Platform messages may fail, so we use a try/catch PlatformException.
    try {
      platformVersion = await FlutterZendeskPlugin.platformVersion;
      _flutterPlugin.init(_accountKey,
          applicationId: _applicationId,
          clientId: _clientId,
          domainUrl: _domainUrl);
    } on PlatformException {
      platformVersion = 'Failed to get platform version.';
    }

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    setState(() {
      _platformVersion = platformVersion;
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
            child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          crossAxisAlignment: CrossAxisAlignment.center,
          children: <Widget>[
            Text('Running on: $_platformVersion\n'),
            Text('Chat status: '),
            RaisedButton(
              onPressed: () async {
                await _flutterPlugin.startChat(
                    phone: "17384725179", name: "HGY", email: "HGY@gmail.com");
              },
              child: Text("Start Chat"),
            ),
            RaisedButton(
              onPressed: () async {
                await _flutterPlugin.helpCenter();
              },
              child: Text("Help Center"),
            ),
          ],
        )),
      ),
    );
  }
}
