package com.awd.plugin.hotspot

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiNetworkSpecifier
import android.os.Build
import androidx.annotation.RequiresApi
import java.lang.ref.WeakReference

class WifiConnector(private val context: WeakReference<Context>) {
    @RequiresApi(Build.VERSION_CODES.Q)
    fun connectToWifi(ssid: String, password: String) {
        val specifier = WifiNetworkSpecifier.Builder()
            .setSsid(ssid)
            .setWpa2Passphrase(password)
            .build()

        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .setNetworkSpecifier(specifier)
            .build()

        val connectivityManager = context.get()!!.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.requestNetwork(request, object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: android.net.Network) {
                // Successfully connected to the network
                println("Connected to $ssid")
                connectivityManager.bindProcessToNetwork(network)
            }

            override fun onUnavailable() {
                // Failed to connect to the network
                println("Failed to connect to $ssid")
            }
        })
    }
}