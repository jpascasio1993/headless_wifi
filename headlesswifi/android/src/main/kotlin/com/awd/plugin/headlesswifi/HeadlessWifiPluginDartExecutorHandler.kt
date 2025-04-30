package com.awd.plugin.headlesswifi

import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel

class HeadlessWifiPluginDartExecutorHandler: MethodChannel.MethodCallHandler {
    override fun onMethodCall(
        call: MethodCall,
        result: MethodChannel.Result
    ) {
        if (call.method == "headless_wifi_dart_executor.initialized") {
            result.success(true)
            return
        }
    }
}