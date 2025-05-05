package com.awd.plugin.headlesswifi

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.service.notification.StatusBarNotification
import androidx.annotation.RequiresPermission
import com.awd.plugin.hotspot.HotspotManager
import com.awd.plugin.hotspot.WebPortal
import com.awd.plugin.hotspot.WifiConnector
import fi.iki.elonen.NanoHTTPD
import io.flutter.plugin.common.MethodChannel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import androidx.core.content.edit
import java.util.concurrent.atomic.AtomicBoolean

class HeadlessWifiPluginService : Service(){
    private val binder = LocalBinder()

    companion object {
        private const val TAG = "HEADLESS_WIFI_PLUGIN_SERVICE"
        private const val WAKELOCK_TAG = "headlesswifi:WAKELOCK_TAG"
        private const val SHAREDPREFERENCE_KEY = "WORKMANAGER_SHARED_PREFERENCE"
        private const val SSID_KEY = "ssid";
        private const val PASSWORD_KEY = "password"
        private const val IS_HIDDEN_NETWORK_KEY = "isHiddenNetwork"
        private const val SERVICE_ID = 1001
        private lateinit var webPortal: WebPortal
        private lateinit var hotspotManager: HotspotManager
        private lateinit var wifiConnector: WifiConnector
        private var hotspotCallback: HotspotManager.HotspotCallback? = null
        private var backgroundChannel: MethodChannel? = null

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
        private fun startHotspot(context: Context) {
            hotspotManager.startHotspot(object: HotspotManager.HotspotCallback {
                override fun onStarted(ssid: String, password: String) {
                    println("$TAG: Hotspot started with SSID: $ssid and Password: $password")
                   try {
                       webPortal.start(NanoHTTPD.SOCKET_READ_TIMEOUT, true)
                   } catch (e: Exception) {
                       println("webPortal failed to start")
                       e.printStackTrace()
                   }
                    hotspotCallback?.onStarted(ssid, password)
                    connectToPreviouslyConnectedWifi(context)
                    notify(context, "SSID: $ssid, Password: $password\nportal: ${hotspotManager.getHotspotLocalIp()}")
                }
            })
        }

        fun saveCredentialsOfConnectedWifi(context: Context, ssid: String, password: String, isHiddenNetwork: Boolean) {
            val sharedPreferences = context.getSharedPreferences(SHAREDPREFERENCE_KEY, Context.MODE_PRIVATE)
            sharedPreferences.edit() {
                putString(SSID_KEY, ssid)
                putString(PASSWORD_KEY, password)
                putBoolean(IS_HIDDEN_NETWORK_KEY, isHiddenNetwork)
            }
        }

        fun connectToPreviouslyConnectedWifi(context: Context) {
            val sharedPreferences = context.getSharedPreferences(SHAREDPREFERENCE_KEY, Context.MODE_PRIVATE)
            val ssid = sharedPreferences.getString(SSID_KEY, null)
            val password = sharedPreferences.getString(PASSWORD_KEY, null)
            val isHiddenNetwork = sharedPreferences.getBoolean(IS_HIDDEN_NETWORK_KEY, false)

            if (ssid != null && password != null) {
                wifiConnector.connectToWifi(ssid, password, isHiddenNetwork, object: WebPortal.PostCallback {
                    override fun onComplete(connected: Boolean, hasInternet: Boolean) {
                        println("Connected to previously connected wifi: $connected")
                        publishStatusToDart(connected, hasInternet)
                    }
                })
            }
        }


        fun notify(context: Context, message: String?) {

            var isForegroundNotificationVisible = false
            val notificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            val notifications = notificationManager.activeNotifications
            isForegroundNotificationVisible = notifications.any {
                it.id == SERVICE_ID
            }

            if(isForegroundNotificationVisible) {
                notificationManager.notify(SERVICE_ID, ServiceNotification.createNotification(context, message))
                return
            }
        }

        fun publishStatusToDart(connected: Boolean, hasInternet: Boolean) {
            CoroutineScope(Dispatchers.Main + SupervisorJob()).launch {
                backgroundChannel?.invokeMethod("headless_wifi.connectToWifi", arrayOf(connected, hasInternet), object : MethodChannel.Result {
                    override fun success(result: Any?) {
                        println("invokeMethod success: $result")
                    }

                    override fun error(errorCode: String, errorMessage: String?, errorDetails: Any?) {
                        println("invokeMethod error: $errorMessage")
                    }

                    override fun notImplemented() {
                        println("invokeMethod notImplemented")
                    }
                })
            }
        }

        fun setBackgroundChannel(channel: MethodChannel?) {
            backgroundChannel = channel
        }

        val hostname: String? get() = webPortal.hostname

        val ip: String? get() = hotspotManager.getHotspotLocalIp()

    }

    override fun onBind(p0: Intent?): IBinder? {
        return binder
    }

    inner class LocalBinder: Binder() {
        fun getService(): HeadlessWifiPluginService {
            return this@HeadlessWifiPluginService
        }
    }





    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.NEARBY_WIFI_DEVICES])
    @SuppressLint("ForegroundServiceType")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        println("$TAG: onStartCommand()")

        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(
                SERVICE_ID,
                ServiceNotification.createNotification(this, null),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        }else {
            startForeground(
                SERVICE_ID,
                ServiceNotification.createNotification(this, null),
            )
        }
        startHotspot(this)
        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        releaseWakeLock()
        stopForegroundService()
        releaseWakeLock()
        try {
            webPortal.stop()
            hotspotManager.stopHotspot()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        super.onTaskRemoved(rootIntent)
        startService(applicationContext)
    }

    override fun onCreate() {
        super.onCreate()
        hotspotManager = HotspotManager(this)
        wifiConnector = WifiConnector(this)

        webPortal = WebPortal(
            object : WebPortal.WebPortListener {
                override fun onCredentialsSubmit(ssid: String, password: String, isHiddenNetwork: Boolean, callback: WebPortal.PostCallback) {
                    println("submitted credentials: $ssid, $password, $isHiddenNetwork")
                    wifiConnector.connectToWifi(ssid, password, isHiddenNetwork, object: WebPortal.PostCallback {
                        override fun onComplete(
                            connected: Boolean,
                            hasInternet: Boolean
                        ) {
                            callback.onComplete(connected, hasInternet)
                            saveCredentialsOfConnectedWifi(this@HeadlessWifiPluginService, ssid, password, isHiddenNetwork)
                            publishStatusToDart(connected, hasInternet)
                        }

                    })
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