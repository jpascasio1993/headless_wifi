package com.awd.plugin.hotspot

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiNetworkSpecifier

class WifiConnector(private val context: Context) {
    fun connectToWifi(ssid: String, password: String, isHiddenNetwork: Boolean, postCallback: WebPortal.PostCallback) {
        val specifier = WifiNetworkSpecifier.Builder()
            .setSsid(ssid)
            .setWpa2Passphrase(password)
            .setIsHiddenSsid(isHiddenNetwork)
            .build()

        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .setNetworkSpecifier(specifier)
            .build()

        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        try {
            connectivityManager.requestNetwork(request, object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    println("Connected to $ssid")
                    val binded = connectivityManager.bindProcessToNetwork(network)
                    println("network binded: $binded")

                    // Verify internet connectivity
                    val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
                    val hasInternet = networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
                    println("Internet access available: $hasInternet")

                    postCallback.onComplete(true, hasInternet)
                }

                override fun onUnavailable() {
                    println("Failed to connect to $ssid")
                    postCallback.onComplete(false, false)
//                    cleanup()
                }

                override fun onLost(network: Network) {
                    super.onLost(network)
                    postCallback.onComplete(false, false)
                    println("Lost connection to $ssid")
                    cleanup()
                }

                private fun cleanup() {
                    connectivityManager.bindProcessToNetwork(null)
                    connectivityManager.unregisterNetworkCallback(this)
                }
            })
        } catch (e: Exception) {
            println("Failed to request network: ${e.message}")
            e.printStackTrace()
        }
    }
}