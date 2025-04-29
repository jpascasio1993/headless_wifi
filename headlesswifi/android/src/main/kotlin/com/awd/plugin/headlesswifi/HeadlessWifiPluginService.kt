package com.awd.plugin.headlesswifi

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.os.IBinder
import com.awd.plugin.hotspot.HotspotManager
import com.awd.plugin.hotspot.WebPortal
import com.awd.plugin.hotspot.WifiConnector
import java.lang.ref.WeakReference
import androidx.core.content.edit
import androidx.core.os.bundleOf

class HeadlessWifiPluginService : Service() {
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

        fun startService(context: Context, ssid: String?, password: String?) {
            val pref = context.getSharedPreferences(SHAREDPREFERENCE_KEY, MODE_PRIVATE)
            println("$TAG: startService($ssid, $password)")
            pref.edit {
                putString(SSID_KEY, ssid)
                    .putString(PASSWORD_KEY, password)
            }

            val restartService = Intent(context, HeadlessWifiPluginService::class.java)
            val bundle = bundleOf(Pair(SSID_KEY, ssid), Pair(PASSWORD_KEY, password))
            restartService.putExtras(bundle)

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

        private fun startHotspot(ssid: String?, password: String?) {
            if (ssid == null || password == null) {
                println("$TAG: SSID or Password is null")
                return
            }
            val success = hotspotManager.startHotspot(ssid, password)
            if (!success) return
            webPortal.start()
        }

    }


    @SuppressLint("ForegroundServiceType")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        println("$TAG: onStartCommand()")
        startForeground(
            SERVICE_ID,
            ServiceNotification.createNotification(WeakReference<Context>(this))
        )
        ssid = intent?.getStringExtra("ssid")
        password = intent?.getStringExtra("password")
//        startHotspot(ssid, password)
        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        releaseWakeLock()
        stopForegroundService()
        releaseWakeLock()
        super.onTaskRemoved(rootIntent)
        startService(applicationContext, ssid, password)
    }

    override fun onCreate() {
        super.onCreate()
        hotspotManager = HotspotManager(WeakReference(this))
        wifiConnector = WifiConnector(WeakReference(this))

        webPortal = WebPortal(
            object : WebPortal.WebPortListener {
                override fun onCredentialsSubmit(ssid: String, password: String) {
                    webPortal.stop()
                    wifiConnector.connectToWifi(ssid, password)
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
        startService(applicationContext, ssid, password)
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