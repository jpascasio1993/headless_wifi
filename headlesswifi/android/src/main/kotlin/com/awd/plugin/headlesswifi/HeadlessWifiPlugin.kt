package com.awd.plugin.headlesswifi

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.JSONMethodCodec
import io.flutter.plugin.common.MethodChannel

/** HeadlesswifiPlugin */
class HeadlessWifiPlugin: FlutterPlugin {
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private lateinit var channel : MethodChannel

  override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "headlesswifi", JSONMethodCodec.INSTANCE)
    channel.setMethodCallHandler(HeadlessWifiPluginHandler(HeadlessWifiPluginDelegate(flutterPluginBinding.applicationContext)))
  }

  override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
  }
}
