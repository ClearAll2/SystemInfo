package com.lkonlesoft.displayinfo.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import android.os.Build
import androidx.glance.appwidget.updateAll
import com.lkonlesoft.displayinfo.widget.BatteryWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class BatteryStatusReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BATTERY_CHANGED) {
            val cycleCount = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
                    intent.getIntExtra(BatteryManager.EXTRA_CYCLE_COUNT, 0).toString()
                else "No Data"
            // Save the battery level into SharedPreferences (the same store that your widget can read)
            val prefs = context.getSharedPreferences(BatteryWidget.PREFS_NAME, Context.MODE_PRIVATE)
            with(prefs.edit()) {
                putString(BatteryWidget.CYCLE_COUNT, cycleCount)
                apply()
            }

            // Request a widget update so that the new battery status is displayed
            CoroutineScope(SupervisorJob() + Dispatchers.Main).launch {
                BatteryWidget().updateAll(context)
            }
        }
    }
}
