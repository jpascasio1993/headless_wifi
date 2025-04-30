package com.awd.plugin.headlesswifi

import android.content.Context
import com.awd.plugin.hotspot.HotspotManager

class HeadlessWifiPluginDelegate(private val context: Context) {
    fun startHeadlessWifi(callback: HotspotManager.HotspotCallback) {
        HeadlessWifiPluginService.setHotspotCallback(callback)
        HeadlessWifiPluginService.startService(context)
    }
    fun stopHeadlessWifi() {
        HeadlessWifiPluginService.stopService(context)
    }

    val hostname: String? get() = HeadlessWifiPluginService.hostname

    val ip: String? get() = HeadlessWifiPluginService.ip
}