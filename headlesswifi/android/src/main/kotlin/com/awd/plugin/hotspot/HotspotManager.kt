package com.awd.plugin.hotspot

import android.annotation.SuppressLint
import android.content.Context
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import java.lang.ref.WeakReference
import java.lang.reflect.Method

class HotspotManager(private val context: WeakReference<Context>) {

    private val wifiManager =
        context.get()!!.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

    @SuppressLint("PrivateApi")
    fun startHotspot(ssid: String, password: String): Boolean {
        try {
            val method: Method = wifiManager.javaClass.getDeclaredMethod(
                "setWifiApEnabled",
                WifiConfiguration::class.java,
                Boolean::class.javaPrimitiveType
            )

            val wifiConfig = WifiConfiguration().apply {
                SSID = ssid
                preSharedKey = password
                allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK or WifiConfiguration.KeyMgmt.WPA2_PSK)
            }
            return method.invoke(wifiManager, wifiConfig, true) as Boolean

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    @SuppressLint("PrivateApi")
    fun stopHotspot(): Boolean {
        try {
            val method: Method = wifiManager.javaClass.getDeclaredMethod(
                "setWifiApEnabled",
                WifiConfiguration::class.java,
                Boolean::class.javaPrimitiveType
            )
            return method.invoke(wifiManager, null, false) as Boolean
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

}