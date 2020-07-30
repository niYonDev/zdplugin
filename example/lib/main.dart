import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:flutter_zendes_plugin/flutter_zendes_plugin.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';
  String _accountkey = 'tFh19KFTd8BBqP3gihF65iF9ep7q4sLa';
  String _applicationId = '6783b5375f399d60da5242b77dc5fa5a888c51d39d841212';
  FlutterZendesPlugin _flutterPlugin = FlutterZendesPlugin();
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
      platformVersion = await FlutterZendesPlugin.platformVersion;
      _flutterPlugin.init("accountKey");
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
                await _flutterPlugin.startChat();
//                await _chatApi.startChat('Test Visitor Name',
//                    department: 'Card');
              },
              child: Text("Start Chat"),
            ),
            RaisedButton(
              onPressed: () async {
                await _flutterPlugin.helpCenter();
              },
              child: Text("Help Center"),
            ),
            RaisedButton(
              onPressed: () async {
//                    final file =
//                    await ImagePicker.pickImage(source: ImageSource.gallery);
//                    if (file != null) {
//                      try {
//                        await _chatApi.sendAttachment(file.path);
//                      } on PlatformException catch (e) {
//                        debugPrint('An error occurred: ${e.code}');
//                      }
//                    }
//                    ;
              },
              child: Text("Send Attachment"),
            ),
            RaisedButton(
              onPressed: () async {
//                    await _chatApi.sendChatRating(ChatRating.GOOD,
//                        comment: 'Good service');
              },
              child: Text("Send GOOD Rating"),
            ),
            RaisedButton(
              onPressed: () async {
//                    await _chatApi.sendChatRating(ChatRating.BAD,
//                        comment: 'Bad service');
              },
              child: Text("Send BAD Rating"),
            ),
            RaisedButton(
              onPressed: () async {
//                    await _chatApi
//                        .sendOfflineMessage('Offline Greeting from Visitor');
              },
              child: Text("Send Offline Message"),
            ),
            RaisedButton(
              onPressed: () async {
//                    await _chatApi.endChat();
              },
              child: Text("EndChat"),
            ),
          ],
        )),
      ),
    );
  }
}
