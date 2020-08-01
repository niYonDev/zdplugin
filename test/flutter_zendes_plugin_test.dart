import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_zendesk_plugin/flutter_zendesk_plugin.dart';

void main() {
  const MethodChannel channel = MethodChannel('flutter_zendes_plugin');

  TestWidgetsFlutterBinding.ensureInitialized();

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('getPlatformVersion', () async {
    expect(await FlutterZendeskPlugin.platformVersion, '42');
  });
}
