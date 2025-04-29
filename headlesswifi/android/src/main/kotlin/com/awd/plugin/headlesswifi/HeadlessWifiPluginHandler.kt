package com.awd.plugin.headlesswifi

import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import org.json.JSONObject

class HeadlessWifiPluginHandler(private val delegate: HeadlessWifiPluginDelegate): MethodChannel.MethodCallHandler {
    override fun onMethodCall(
        call: MethodCall,
        result: MethodChannel.Result
    ) {
        val method = call.method
        val arguments = call.arguments
        when (method) {
            "getPlatformVersion" -> {
                result.success("Android ${android.os.Build.VERSION.RELEASE}")
                return
            }
            "startHeadlessWifi" -> {
                val params = arguments as JSONObject
                delegate.startHeadlessWifi(
                    params["ssid"] as String,
                    params["password"] as String
                )
                result.success(true)
                return
            }
            "stopHeadlessWifi" -> {
                delegate.stopHeadlessWifi()
                result.success(true)
                return
            }
            else -> {
                result.notImplemented()
            }
        }
    }
}