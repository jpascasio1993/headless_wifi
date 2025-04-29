import 'package:flutter_test/flutter_test.dart';
import 'package:headlesswifi/headlesswifi.dart';
import 'package:headlesswifi/headlesswifi_platform_interface.dart';
import 'package:headlesswifi/headlesswifi_method_channel.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockHeadlesswifiPlatform
    with MockPlatformInterfaceMixin
    implements HeadlesswifiPlatform {

  @override
  Future<String?> getPlatformVersion() => Future.value('42');
}

void main() {
  final HeadlesswifiPlatform initialPlatform = HeadlesswifiPlatform.instance;

  test('$MethodChannelHeadlesswifi is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelHeadlesswifi>());
  });

  test('getPlatformVersion', () async {
    Headlesswifi headlesswifiPlugin = Headlesswifi();
    MockHeadlesswifiPlatform fakePlatform = MockHeadlesswifiPlatform();
    HeadlesswifiPlatform.instance = fakePlatform;

    expect(await headlesswifiPlugin.getPlatformVersion(), '42');
  });
}
