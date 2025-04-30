import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'headlesswifi_method_channel.dart';

abstract class HeadlesswifiPlatform extends PlatformInterface {
  /// Constructs a HeadlesswifiPlatform.
  HeadlesswifiPlatform() : super(token: _token);

  static final Object _token = Object();

  static HeadlesswifiPlatform _instance = MethodChannelHeadlesswifi();

  /// The default instance of [HeadlesswifiPlatform] to use.
  ///
  /// Defaults to [MethodChannelHeadlesswifi].
  static HeadlesswifiPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [HeadlesswifiPlatform] when
  /// they register themselves.
  static set instance(HeadlesswifiPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String?> getPlatformVersion() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }

  Future<Map<String, dynamic>?> startWifi();

  Future<bool> stopWifi();
}
