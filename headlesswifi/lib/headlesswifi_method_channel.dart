import 'dart:io';
import 'dart:ui';

import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';

import 'headlesswifi_platform_interface.dart';

// @pragma('vm:entry-point')
// void dartBackgroundCallback() {
//   const MethodChannel _channel = MethodChannel(
//     'headless_wifi_dart_executor',
//     JSONMethodCodec(),
//   );

//   // Setup Flutter state needed for MethodChannels.
//   WidgetsFlutterBinding.ensureInitialized();

//   // This is where the magic happens and we handle background events from the
//   // native portion of the plugin.
//   _channel.setMethodCallHandler((MethodCall call) async {
//     final dynamic args = call.arguments;
//     //final dynamic callbackId = (args as List).length > 1 ? args[1] : -1;
//     final CallbackHandle handle = CallbackHandle.fromRawHandle(args[0]);

//     // PluginUtilities.getCallbackFromHandle performs a lookup based on the
//     // callback handle and returns a tear-off of the original callback.
//     final Function? closure = PluginUtilities.getCallbackFromHandle(handle);
//     debugPrint('gonna check closure');
//     if (closure == null) {
//       debugPrint('Fatal: could not find callback');
//       exit(-1);
//     }
//     await closure();
//   });

//   _channel.invokeMethod<void>('headless_wifi_dart_executor.initialized');
// }

/// An implementation of [HeadlesswifiPlatform] that uses method channels.
class MethodChannelHeadlesswifi extends HeadlesswifiPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('headlesswifi', JSONMethodCodec());

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
