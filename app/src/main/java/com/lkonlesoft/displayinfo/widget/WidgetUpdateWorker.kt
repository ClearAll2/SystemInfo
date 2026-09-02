package com.lkonlesoft.displayinfo.widget

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.updateAll
import androidx.work.BackoffPolicy
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit

class WidgetUpdateWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        BatteryWidget().updateAll(this.applicationContext)
        updateWidgetPreview(this.applicationContext)
        return Result.success()
    }

    suspend fun updateWidgetPreview (context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM)
            return

        val manager = GlanceAppWidgetManager(context)
        val result = manager.setWidgetPreviews(BatteryWidgetReceiver::class)
        when (result) {
            GlanceAppWidgetManager.SET_WIDGET_PREVIEWS_RESULT_SUCCESS -> {
                Log.d("WidgetUpdateWorker", "Preview published")
            }
            GlanceAppWidgetManager.SET_WIDGET_PREVIEWS_RESULT_RATE_LIMITED -> {
                Log.d("WidgetUpdateWorker", "Preview rate limited")
            }
        }
    }

    companion object{
        fun enqueueWork(context: Context){
            val request = PeriodicWorkRequestBuilder<WidgetUpdateWorker>(180, TimeUnit.MINUTES)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.MINUTES)
                .addTag("battery_widget")
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "battery_widget",
                ExistingPeriodicWorkPolicy.REPLACE,
                request
            )
        }
    }
}