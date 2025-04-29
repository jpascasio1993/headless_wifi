import 'headlesswifi_platform_interface.dart';

class Headlesswifi {
  Headlesswifi();

  Future<String?> getPlatformVersion() {
    return HeadlesswifiPlatform.instance.getPlatformVersion();
  }

  Future<bool> startWifi({required String ssid, required String password}) {
    return HeadlesswifiPlatform.instance.startWifi(
      ssid: ssid,
      password: password,
    );
  }

  Future<bool> stopWifi() {
    return HeadlesswifiPlatform.instance.stopWifi();
  }
}
