import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'headlesswifi_platform_interface.dart';

/// An implementation of [HeadlesswifiPlatform] that uses method channels.
class MethodChannelHeadlesswifi extends HeadlesswifiPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('headlesswifi', JSONMethodCodec());

  @override
  Future<String?> getPlatformVersion() async {
    final version = await methodChannel.invokeMethod<String>(
      'getPlatformVersion',
    );
    return version;
  }

  @override
  Future<bool> startWifi({
    required String ssid,
    required String password,
  }) async {
    final res = await methodChannel.invokeMethod('startHeadlessWifi', {
      'ssid': ssid,
      'password': password,
    });
    return res;
  }

  @override
  Future<bool> stopWifi() async {
    final res = await methodChannel.invokeMethod('stopHeadlessWifi');
    return res;
  }
}
