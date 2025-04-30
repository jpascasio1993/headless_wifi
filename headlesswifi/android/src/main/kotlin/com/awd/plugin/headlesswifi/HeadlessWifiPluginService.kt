package com.awd.plugin.headlesswifi

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.os.IBinder
import androidx.annotation.RequiresPermission
import com.awd.plugin.hotspot.HotspotManager
import com.awd.plugin.hotspot.WebPortal
import com.awd.plugin.hotspot.WifiConnector
import java.lang.ref.WeakReference
import androidx.core.content.edit
import androidx.core.os.bundleOf
import com.awd.plugin.headlesswifi.HeadlessWifiPluginService.Companion.hotspotCallback
import fi.iki.elonen.NanoHTTPD

class HeadlessWifiPluginService : Service(){
    private var ssid: String? = null
    private var password: String? = null

    override fun onBind(p0: Intent?): IBinder? {
        // Return null binder since there won't be
        // any activity calling this service
        return null
    }

    companion object {
        private const val TAG = "HEADLESS_WIFI_PLUGIN_SERVICE"
        private const val WAKELOCK_TAG = "headlesswifi:WAKELOCK_TAG"
        private const val SHAREDPREFERENCE_KEY = "WORKMANAGER_SHARED_PREFERENCE"
        private const val SSID_KEY = "ssid";
        private const val PASSWORD_KEY = "password"
        private const val SERVICE_ID = 1001
        private lateinit var webPortal: WebPortal
        private lateinit var hotspotManager: HotspotManager
        private lateinit var wifiConnector: WifiConnector
        private lateinit var hotspotCallback: HotspotManager.HotspotCallback

        fun startService(context: Context) {

            val restartService = Intent(context, HeadlessWifiPluginService::class.java)

            val pendintIntentFlag =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0

            val restartServicePI = PendingIntent.getService(
                context,
                1,
                restartService,
                pendintIntentFlag
            )

            val alarmService = context.getSystemService(ALARM_SERVICE) as AlarmManager

            val triggerAtMillis = System.currentTimeMillis() + 1000

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmService.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    restartServicePI
                )
                return
            }
            alarmService.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, restartServicePI)
        }

        fun stopService(context: Context) {
            val serviceIntent = Intent(context, HeadlessWifiPluginService::class.java)
            context.stopService(serviceIntent)
        }

        fun setHotspotCallback(callback: HotspotManager.HotspotCallback) {
            hotspotCallback = callback
        }

        @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.NEARBY_WIFI_DEVICES])
        private fun startHotspot() {
            hotspotManager.startHotspot(object: HotspotManager.HotspotCallback {
                override fun onStarted(ssid: String, password: String) {
                    println("$TAG: Hotspot started with SSID: $ssid and Password: $password")
                   try {
                       webPortal.start(NanoHTTPD.SOCKET_READ_TIMEOUT, true)
                   } catch (e: Exception) {
                       println("webPortal failed to start")
                       e.printStackTrace()
                   }
                    hotspotCallback.onStarted(ssid, password)
                }
            })
        }

        val hostname: String? get() = webPortal.hostname

        val ip: String? get() = hotspotManager.getHotspotLocalIp()
    }


    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.NEARBY_WIFI_DEVICES])
    @SuppressLint("ForegroundServiceType")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        println("$TAG: onStartCommand()")
        startForeground(
            SERVICE_ID,
            ServiceNotification.createNotification(WeakReference<Context>(this))
        )
        startHotspot()
        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        releaseWakeLock()
        stopForegroundService()
        releaseWakeLock()
        super.onTaskRemoved(rootIntent)
        startService(applicationContext)
    }

    override fun onCreate() {
        super.onCreate()
        hotspotManager = HotspotManager(WeakReference(this))
        wifiConnector = WifiConnector(WeakReference(this))

        webPortal = WebPortal(
            object : WebPortal.WebPortListener {
                override fun onCredentialsSubmit(ssid: String, password: String, isHiddenNetwork: Boolean, callback: WebPortal.PostCallback) {
//                    webPortal.stop()
                    println("submitted credentials: $ssid, $password, $isHiddenNetwork")
                    wifiConnector.connectToWifi(ssid, password, isHiddenNetwork, callback)
                }
            }
        )
        println("$TAG: onCreate()")
        acquireWakeLock()
    }

    override fun onDestroy() {
        stopForegroundService()
        releaseWakeLock()
        try {
            webPortal.stop()
            hotspotManager.stopHotspot()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        super.onDestroy()
        startService(applicationContext)
    }

    private fun stopForegroundService() {
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    @SuppressLint("WakelockTimeout")
    fun acquireWakeLock() {
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        releaseWakeLock()
        val wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKELOCK_TAG)
        wakeLock.setReferenceCounted(false)
        wakeLock.acquire()
    }

    fun releaseWakeLock() {
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKELOCK_TAG)
        if (wakeLock.isHeld) {
            wakeLock.release();
        }
    }
}