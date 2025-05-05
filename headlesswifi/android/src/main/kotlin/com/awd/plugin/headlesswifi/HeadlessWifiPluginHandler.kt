package com.awd.plugin.headlesswifi

import com.awd.plugin.hotspot.HotspotManager
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import org.json.JSONObject

class HeadlessWifiPluginHandler(private val delegate: HeadlessWifiPluginDelegate): MethodChannel.MethodCallHandler {
    private var methodChannelResult: MethodChannel.Result? = null

    fun cleanUp() {
        methodChannelResult = null
    }
    override fun onMethodCall(
        call: MethodCall,
        result: MethodChannel.Result
    ) {
        methodChannelResult = result
        val method = call.method
        when (method) {
            "getPlatformVersion" -> {
                methodChannelResult?.success("Android ${android.os.Build.VERSION.RELEASE}")
                return
            }
            "startHeadlessWifi" -> {
                delegate.startHeadlessWifi(object: HotspotManager.HotspotCallback {
                    override fun onStarted(ssid: String, password: String) {
                        println("hostname ${delegate.hostname}")
                        methodChannelResult?.success(JSONObject()
                            .put("ssid", ssid)
                            .put("password", password)
                            .put("ip", delegate.ip)
                            .put("hostname", delegate.hostname))
                    }
                })
                return
            }
            "stopHeadlessWifi" -> {
                delegate.stopHeadlessWifi()
                methodChannelResult?.success(true)
                return
            }
            else -> {
                methodChannelResult?.notImplemented()
            }
        }
    }
}