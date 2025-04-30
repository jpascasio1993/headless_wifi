import 'headlesswifi_platform_interface.dart';

class Headlesswifi {
  Headlesswifi();

  Future<String?> getPlatformVersion() {
    return HeadlesswifiPlatform.instance.getPlatformVersion();
  }

  Future<Map<String, dynamic>?> startWifi() {
    return HeadlesswifiPlatform.instance.startWifi();
  }

  Future<bool> stopWifi() {
    return HeadlesswifiPlatform.instance.stopWifi();
  }
}
