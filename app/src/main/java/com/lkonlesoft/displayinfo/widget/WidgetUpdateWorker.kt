package com.lkonlesoft.displayinfo.widget

import android.content.Context
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
        return Result.success()
    }

    companion object{
        fun enqueueWork(context: Context){
            val request = PeriodicWorkRequestBuilder<WidgetUpdateWorker>(360, TimeUnit.MINUTES)
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