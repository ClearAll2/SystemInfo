package com.lkonlesoft.displayinfo.utils

import android.content.Context
import android.os.Build
import android.os.SystemClock
import android.provider.Settings
import androidx.annotation.RequiresApi
import java.io.File
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

    fun getSelinuxStatus(): String {
        return try {
            val process = Runtime.getRuntime().exec("getenforce")
            process.inputStream.bufferedReader().readLine()
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }

    fun isUsbDebuggingEnabled(context: Context): Boolean {
        return Settings.Global.getInt(
            context.contentResolver,
            Settings.Global.ADB_ENABLED, 0
        ) == 1
    }

    fun getSystemProperty(key: String): String? {
        return try {
            Runtime.getRuntime()
                .exec("getprop $key")
                .inputStream
                .bufferedReader()
                .use { it.readLine() }
                ?.trim()
        } catch (_: Exception) {
            null
        }
    }


    fun isTrebleSupported(): Boolean {
        return try {
            val trebleEnabled = getSystemProperty("ro.treble.enabled")
            if (trebleEnabled != null) {
                trebleEnabled.equals("true", ignoreCase = true)
            } else {
                val vendorFingerprint = getSystemProperty("ro.vendor.build.fingerprint")
                !vendorFingerprint.isNullOrEmpty()
            }
        } catch (_: Exception) {
            false
        }
    }

    fun isSeamlessUpdateSupported(): Boolean {
        // Check the legacy A/B update flag.
        val abUpdate = getSystemProperty("ro.build.ab_update")
        if (abUpdate?.equals("true", ignoreCase = true) == true) {
            return true
        }

        // Check for Virtual A/B support, which is common on Android 11+ devices.
        val virtualAb = getSystemProperty("ro.virtual_ab.enabled")
        if (virtualAb?.equals("true", ignoreCase = true) == true) {
            return true
        }

        // Fallback: If an active slot is defined, it suggests the device operates with A/B partitioning.
        val slot = getSystemProperty("ro.boot.slot_suffix")
        return !slot.isNullOrEmpty()

        // If none of the checks are positive, seamless updates likely aren't supported.
    }


    fun getActiveSlot(): String {
        return try {
            getSystemProperty("ro.boot.slot_suffix") ?: "Unknown"
        } catch (_: Exception) {
            "Unknown"
        }
    }

    fun isDeviceRooted(): Boolean {
        val paths = arrayOf(
            "/system/bin/su", "/system/xbin/su", "/sbin/su",
            "/system/su", "/system/bin/.ext/.su", "/system/usr/we-need-root/su"
        )
        return paths.any { path -> File(path).exists() }
    }


}