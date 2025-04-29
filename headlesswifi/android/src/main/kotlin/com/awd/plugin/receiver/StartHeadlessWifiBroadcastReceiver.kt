package com.awd.plugin.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.awd.plugin.headlesswifi.HeadlessWifiPluginService

class StartHeadlessWifiBroadcastReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if(context == null) return
        HeadlessWifiPluginService.startService(context, null, null)
    }
}