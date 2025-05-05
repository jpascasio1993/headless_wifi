import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';

import 'headlesswifi_platform_interface.dart';

/// An implementation of [HeadlesswifiPlatform] that uses method channels.
class MethodChannelHeadlesswifi extends HeadlesswifiPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('headlesswifi', JSONMethodCodec());

  /// The method channel used to listen for events from the native platform
  /// and send them to the Dart side.
  final backgroundChannel = const MethodChannel(
    'headless_wifi_dart_executor',
    JSONMethodCodec(),
  );

  @override
  Future<String?> getPlatformVersion() async {
    final version = await methodChannel.invokeMethod<String>(
      'getPlatformVersion',
    );
    return version;
  }

  @override
  Future<Map<String, dynamic>?> startWifi() async {
    final res = await methodChannel.invokeMethod<Map<String, dynamic>>(
      'startHeadlessWifi',
    );
    return res;
  }

  @override
  Future<bool> stopWifi() async {
    final res = await methodChannel.invokeMethod('stopHeadlessWifi');
    return res;
  }

  @override
  void listenForWifiEvent(WifiEventListener onEvent) {
    backgroundChannel.setMethodCallHandler((MethodCall call) async {
      onEvent(call.arguments[0], call.arguments[1]);
    });
  }
}
