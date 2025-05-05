package com.awd.plugin.headlesswifi

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.JSONMethodCodec
import io.flutter.plugin.common.MethodChannel

/** HeadlesswifiPlugin */
class HeadlessWifiPlugin: FlutterPlugin, ActivityAware{
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private lateinit var channel : MethodChannel
  private lateinit var backgroundChannel: MethodChannel
  private lateinit var handler: HeadlessWifiPluginHandler

  override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {

    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "headlesswifi", JSONMethodCodec.INSTANCE)
    backgroundChannel = MethodChannel(flutterPluginBinding.binaryMessenger, "headless_wifi_dart_executor", JSONMethodCodec.INSTANCE)
    handler = HeadlessWifiPluginHandler(HeadlessWifiPluginDelegate(flutterPluginBinding.applicationContext))
    channel.setMethodCallHandler(handler)
    backgroundChannel.setMethodCallHandler(HeadlessWifiPluginDartExecutorHandler())
    HeadlessWifiPluginService.setBackgroundChannel(backgroundChannel)
  }

  override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
    HeadlessWifiPluginService.setBackgroundChannel(null)
  }

  override fun onAttachedToActivity(binding: ActivityPluginBinding) {
  }

  override fun onDetachedFromActivityForConfigChanges() {
    handler.cleanUp()
    HeadlessWifiPluginService.setBackgroundChannel(null)

  }

  override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
  }

  override fun onDetachedFromActivity() {
    handler.cleanUp()
  }
}
