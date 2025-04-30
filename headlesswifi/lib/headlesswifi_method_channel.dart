import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';

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
}
