package com.awd.plugin.hotspot

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.net.wifi.WifiManager
import android.os.Handler
import android.os.Looper
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.Inet4Address
import java.net.NetworkInterface

class HotspotManager(private val context: Context) {
    private var ssid: String? = null
    private var password: String? = null

    interface HotspotCallback {
        fun onStarted(ssid: String, password: String)
    }

    private val wifiManager =
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    private var localHotspotReservation: WifiManager.LocalOnlyHotspotReservation? = null


    @RequiresPermission(allOf = [Manifest.permission.NEARBY_WIFI_DEVICES, Manifest.permission.ACCESS_FINE_LOCATION])
    @SuppressLint("PrivateApi", "MissingPermission")
    fun startHotspot(callback: HotspotCallback) {
        if(localHotspotReservation != null) {
            println("hotspot already started")
            callback.onStarted(ssid!!, password!!)
            return
        }

        try {
            CoroutineScope(Dispatchers.Main).launch {
                wifiManager.startLocalOnlyHotspot(object: WifiManager.LocalOnlyHotspotCallback() {
                    override fun onStarted(reservation: WifiManager.LocalOnlyHotspotReservation) {
                        super.onStarted(reservation)
                        val config = reservation.softApConfiguration
                        ssid = config.wifiSsid!!.toString()
                        password = config.passphrase!!
                        callback.onStarted(ssid!!, password!!)
                        localHotspotReservation = reservation
                    }

                    override fun onStopped() {
                        super.onStopped()
                        println("hotspot stopped")
                    }

                    override fun onFailed(reason: Int) {
                        super.onFailed(reason)
                        println("hotspot failed: $reason")
                    }
                }, Handler(Looper.getMainLooper()))
            }


        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("PrivateApi")
    fun stopHotspot(): Boolean {
        localHotspotReservation?.close()
        localHotspotReservation = null
        return false
    }

    fun getHotspotLocalIp(): String? {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            for (intf in interfaces) {
                val addresses = intf.inetAddresses
                for (addr in addresses) {
                    if (!addr.isLoopbackAddress && addr is Inet4Address) {
                        val ip = addr.hostAddress
                        if(!ip.isNullOrEmpty()) {
                            return ip
                        }
                        // if (ip != null) {
                        //     if (ip.startsWith("192.") || ip.startsWith("172.") || ip.startsWith("10.")) {
                        //         return ip
                        //     }
                        // }
                    }
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return null
    }

}