package com.lkonlesoft.displayinfo.utils

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import java.io.File

object BatteryUtils {

    fun getBatteryCapacity(context: Context): Double {
        return try {
            val powerProfileClass = Class.forName("com.android.internal.os.PowerProfile")
            val constructor = powerProfileClass.getConstructor(Context::class.java)
            val powerProfile = constructor.newInstance(context)
            val method = powerProfileClass.getMethod("getBatteryCapacity")
            method.invoke(powerProfile) as Double
        } catch (_: Exception) {
            -1.0
        }
    }


    // 🔋 Battery Health
    private val healthMap = mapOf(
        BatteryManager.BATTERY_HEALTH_GOOD to "Good",
        BatteryManager.BATTERY_HEALTH_OVERHEAT to "Overheat",
        BatteryManager.BATTERY_HEALTH_DEAD to "Dead",
        BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE to "Over Voltage",
        BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE to "Failure",
        BatteryManager.BATTERY_HEALTH_COLD to "Cold",
        BatteryManager.BATTERY_HEALTH_UNKNOWN to "Unknown"
    )

    fun getBatteryHealth(context: Context): String {
        val intent = getBatteryIntent(context)
        val health = intent?.getIntExtra(BatteryManager.EXTRA_HEALTH, -1) ?: -1
        return healthMap[health] ?: "Other"
    }

    fun getBatteryTechnology(context: Context): String {
        val intent = getBatteryIntent(context)
        return intent?.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Unknown"
    }

    // ⚡️ Battery Status (Charging, Full, etc.)
    private val statusMap = mapOf(
        BatteryManager.BATTERY_STATUS_CHARGING to "Charging",
        BatteryManager.BATTERY_STATUS_DISCHARGING to "Discharging",
        BatteryManager.BATTERY_STATUS_FULL to "Full",
        BatteryManager.BATTERY_STATUS_NOT_CHARGING to "Not Charging",
        BatteryManager.BATTERY_STATUS_UNKNOWN to "Unknown"
    )

    fun getBatteryStatus(context: Context): String {
        val intent = getBatteryIntent(context)
        val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        return statusMap[status] ?: "Other"
    }

    // 🔌 Plug Type (AC, USB, etc.)
    private val plugMap = mapOf(
        BatteryManager.BATTERY_PLUGGED_AC to "AC",
        BatteryManager.BATTERY_PLUGGED_USB to "USB",
        BatteryManager.BATTERY_PLUGGED_WIRELESS to "Wireless"
    )

    fun getPlugType(context: Context): String {
        val intent = getBatteryIntent(context)
        val plugType = intent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) ?: -1
        return plugMap[plugType] ?: "Unplugged"
    }

    // 🔋 Battery Level %
    fun getBatteryPercentage(context: Context): Int {
        val intent = getBatteryIntent(context)
        val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        return if (level >= 0 && scale > 0) (level * 100 / scale) else -1
    }

    // 🌡️ Temperature in °C
    fun getBatteryTemperature(context: Context): Float {
        val intent = getBatteryIntent(context)
        val temp = intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) ?: -1
        return if (temp > 0) temp / 10f else -1f
    }

    fun getBatteryCycleCount(context: Context): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val intent = getBatteryIntent(context)
            intent?.getIntExtra(BatteryManager.EXTRA_CYCLE_COUNT, -1) ?: -1
        } else {
            -1
        }
    }

    fun getChargingCurrent(): Int {
        return try {
            val path = "/sys/class/power_supply/battery/current_now"
            File(path).readText().trim().toInt() / 1000 // Convert μA to mA
        } catch (e: Exception) {
            -1
        }
    }

    fun getChargingVoltage(): Float {
        return try {
            val path = "/sys/class/power_supply/battery/voltage_now"
            File(path).readText().trim().toFloat() / 1000000f // Convert μV to V
        } catch (e: Exception) {
            -1f
        }
    }


    // 🧪 Private helper
    private fun getBatteryIntent(context: Context): Intent? {
        val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        return context.registerReceiver(null, intentFilter)
    }
}