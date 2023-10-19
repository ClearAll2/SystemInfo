package com.lkonlesoft.displayinfo.receiver

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import android.os.Build

class BatteryBroadcastReceiver : BroadcastReceiver() {

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context, intent: Intent) {
        val cycleCount = if (Build.VERSION.SDK_INT >= 34) {
            intent.getStringExtra(BatteryManager.EXTRA_CYCLE_COUNT)
        }
        else {
            "This feature requires Android 14"
        }
        val temp = intent.getStringExtra(BatteryManager.EXTRA_TEMPERATURE)
        val volt = intent.getStringExtra(BatteryManager.EXTRA_VOLTAGE)
        val tech = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY)
    }
}