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

class _MyAppState extends State<MyApp> with WidgetsBindingObserver {
  String _platformVersion = 'Unknown';
  String _accountKey = '';
  String _applicationId = '';
  String _clientId = '';
  String _domainUrl = '';
  FlutterZendeskPlugin _flutterPlugin = FlutterZendeskPlugin();

  @override
  void initState() {
    super.initState();
    initPlatformState();
    //添加生命周期观察者
    WidgetsBinding.instance.addObserver(this);
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    super.didChangeAppLifecycleState(state);
    debugPrint("didChangeAppLifecycleState >>>> " + state.toString());
    if (state == AppLifecycleState.resumed) {
      _flutterPlugin.changeNavStatusAction(false);
    } else {
      _flutterPlugin.changeNavStatusAction(true);
    }
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    String platformVersion;
    // Platform messages may fail, so we use a try/catch PlatformException.
    try {
      platformVersion = await FlutterZendeskPlugin.platformVersion;
      _flutterPlugin.init(
        _accountKey,
        applicationId: _applicationId,
        clientId: _clientId,
        domainUrl: _domainUrl,
        nameIdentifier: "Grayson Identifier",
        emailIdentifier: "Grayson@gmail.com",
        phone: "123121515",
        name: "HGY iOSsdsd",
        email: "HGYiOSsdsd@gmail.com",
        departmentName: "Department Name iOS",
      );
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
                    await _flutterPlugin.startChatV1();
                  },
                  child: Text("Start Chat V1"),
                ),
                RaisedButton(
                  onPressed: () async {
                    await _flutterPlugin.startChatV2(
                        phone: "173****5179",
                        name: "HGY New",
                        email: "HGY New@gmail.com",
                        botLabel: "BR Play Label",
                        departmentName: " Department Name",
                        endChatSwitch: false,
                        toolbarTitle: "Online Service");
                  },
                  child: Text("Start Chat V2"),
                ),
                RaisedButton(
                  onPressed: () async {
                    await _flutterPlugin.helpCenter().then((value) {
                      print('object<<<<<<<<<<<<< ' + value.toString());
                    });
                  },
                  child: Text("Help Center"),
                ),
                RaisedButton(
                  onPressed: () async {
                    await _flutterPlugin.requestListViewAction().then((value) {
                      print('object<<<<<<<<<<<<< ' + value.toString());
                    });
                  },
                  child: Text("Request List"),
                ),
              ],
            )),
        floatingActionButton: FloatingActionButton(
          onPressed: () async {
            await _flutterPlugin.requestViewAction();
          },
          child: Icon(Icons.chat),
        ),
      ),
    );
  }
}
