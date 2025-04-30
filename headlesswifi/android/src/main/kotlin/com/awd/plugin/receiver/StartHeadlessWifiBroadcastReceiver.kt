package com.awd.plugin.receiver

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.awd.plugin.headlesswifi.HeadlessWifiPluginService

class StartHeadlessWifiBroadcastReceiver: BroadcastReceiver() {

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context?, intent: Intent?) {
        if(context == null) return
        HeadlessWifiPluginService.startService(context)
    }
}