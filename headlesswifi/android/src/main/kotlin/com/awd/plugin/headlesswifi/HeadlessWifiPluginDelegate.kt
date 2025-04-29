package com.awd.plugin.headlesswifi

import android.content.Context

class HeadlessWifiPluginDelegate(private val context: Context) {
    fun startHeadlessWifi(ssid: String, password: String) {
        HeadlessWifiPluginService.startService(context, ssid, password)
    }
    fun stopHeadlessWifi() {
        HeadlessWifiPluginService.stopService(context)
    }
}