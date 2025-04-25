package com.lkonlesoft.displayinfo.utils

import android.content.Context
import android.os.Build
import android.os.SystemClock
import androidx.annotation.RequiresApi
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object SystemUtils {
    fun getModel(): String {
        return Build.MODEL ?: "Unknown"
    }

    fun getProduct(): String {
        return Build.PRODUCT ?: "Unknown"
    }

    fun getDevice(): String {
        return Build.DEVICE ?: "Unknown"
    }

    fun getBoard(): String {
        return Build.BOARD ?: "Unknown"
    }

    fun getManufacturer(): String {
        return Build.MANUFACTURER ?: "Unknown"
    }

    fun getBrand(): String {
        return Build.BRAND ?: "Unknown"
    }

    @RequiresApi(Build.VERSION_CODES.S)
    fun getSku(): String {
        return Build.SKU
    }

    fun getRadio(): String {
        return Build.getRadioVersion() ?: "Unknown"
    }

    fun getInstructions(): String {
        val supportedABIS = Build.SUPPORTED_ABIS
        return supportedABIS?.joinToString(", ") ?: "Unknown"
    }

    fun getUptime(): String {
        val uptimeMillis = SystemClock.elapsedRealtime()
        val seconds = uptimeMillis / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24

        return "${days}d ${hours % 24}h ${minutes % 60}m"
    }

    fun getBootTime(): String {
        val bootTime = System.currentTimeMillis() - SystemClock.elapsedRealtime()
        val date = Date(bootTime)
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return format.format(date)
    }

    fun getAllSystemFeatures(context: Context): List<String> {
        val pm = context.packageManager
        return pm.systemAvailableFeatures
            .mapNotNull { it?.name }  // filter out nulls (some features don't have a name)
            .sorted()
    }

}