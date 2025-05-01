package com.lkonlesoft.displayinfo.utils

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import com.lkonlesoft.displayinfo.R
import com.lkonlesoft.displayinfo.helper.DeviceInfo

class BatteryUtils (private val context: Context) {

    fun getBatteryCapacity(): Double {
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
        BatteryManager.BATTERY_HEALTH_GOOD to context.getString(R.string.good),
        BatteryManager.BATTERY_HEALTH_OVERHEAT to context.getString(R.string.overheat),
        BatteryManager.BATTERY_HEALTH_DEAD to context.getString(R.string.dead),
        BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE to context.getString(R.string.over_voltage),
        BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE to context.getString(R.string.failure),
        BatteryManager.BATTERY_HEALTH_COLD to context.getString(R.string.cold),
        BatteryManager.BATTERY_HEALTH_UNKNOWN to context.getString(R.string.unknown)
    )

    fun getBatteryHealth(): String {
        val intent = getBatteryIntent()
        val health = intent?.getIntExtra(BatteryManager.EXTRA_HEALTH, -1) ?: -1
        return healthMap[health] ?: context.getString(R.string.other)
    }

    fun getBatteryTechnology(): String {
        val intent = getBatteryIntent()
        return intent?.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: context.getString(R.string.unknown)
    }

    // ⚡️ Battery Status (Charging, Full, etc.)
    private val statusMap = mapOf(
        BatteryManager.BATTERY_STATUS_CHARGING to context.getString(R.string.charging),
        BatteryManager.BATTERY_STATUS_DISCHARGING to context.getString(R.string.discharging),
        BatteryManager.BATTERY_STATUS_FULL to context.getString(R.string.full),
        BatteryManager.BATTERY_STATUS_NOT_CHARGING to context.getString(R.string.not_charging),
        BatteryManager.BATTERY_STATUS_UNKNOWN to context.getString(R.string.unknown)
    )

    fun getBatteryStatus(): String {
        val intent = getBatteryIntent()
        val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        return statusMap[status] ?: context.getString(R.string.other)
    }

    // 🔌 Plug Type (AC, USB, etc.)
    private val plugMap = mapOf(
        BatteryManager.BATTERY_PLUGGED_AC to context.getString(R.string.ac),
        BatteryManager.BATTERY_PLUGGED_USB to context.getString(R.string.usb),
        BatteryManager.BATTERY_PLUGGED_WIRELESS to context.getString(R.string.wireless)
    )

    fun getPlugType(): String {
        val intent = getBatteryIntent()
        val plugType = intent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) ?: -1
        return plugMap[plugType] ?: context.getString(R.string.unplugged)
    }

    // 🔋 Battery Level %
    fun getBatteryPercentage(): Int {
        val intent = getBatteryIntent()
        val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        return if (level >= 0 && scale > 0) (level * 100 / scale) else -1
    }

    // 🌡️ Temperature in °C
    fun getBatteryTemperature(): Float {
        val intent = getBatteryIntent()
        val temp = intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) ?: -1
        return if (temp > 0) temp / 10f else -1f
    }

    fun getBatteryCycleCount(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val intent = getBatteryIntent()
            intent?.getIntExtra(BatteryManager.EXTRA_CYCLE_COUNT, -1) ?: -1
        } else {
            -1
        }
    }

    fun getDischargeCurrent(): Int {
        val batteryManager = context.applicationContext.getSystemService(Context.BATTERY_SERVICE) as BatteryManager

        // Value in microamperes (µA); negative = discharging, positive = charging
        val currentMicroAmps = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)

        return if (currentMicroAmps != Int.MIN_VALUE) {
            currentMicroAmps.div(1000)
        } else {
            -1 // Unsupported on this device
        }
    }

    fun getChargingVoltage(): Float {
        val intent = getBatteryIntent()
        val temp = intent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1) ?: -1
        return if (temp > 0) temp / 1000f else -1f
    }


    // 🧪 Private helper
    private fun getBatteryIntent(): Intent? {
        val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        return context.registerReceiver(null, intentFilter)
    }

    fun getAllData(): List<DeviceInfo> {
        return listOf(
            DeviceInfo(R.string.status, getBatteryStatus()),
            DeviceInfo(R.string.capacity, getBatteryCapacity().toInt(), " mAh"),
            DeviceInfo(R.string.battery_level, getBatteryPercentage(), "%"),
            DeviceInfo(R.string.health, getBatteryHealth()),
            DeviceInfo(R.string.cycle_count, getBatteryCycleCount()),
            DeviceInfo(R.string.temperature, getBatteryTemperature(), " °C"),
            DeviceInfo(R.string.current, getDischargeCurrent(), " mA"),
            DeviceInfo(R.string.voltage, getChargingVoltage(), " V"),
            DeviceInfo(R.string.plug_type, getPlugType()),
            DeviceInfo(R.string.technology, getBatteryTechnology()),
        )
    }

    fun getDashboardData(): List<DeviceInfo> {
        return listOf(
            DeviceInfo(R.string.battery_level, getBatteryPercentage(), "%"),
            DeviceInfo(R.string.status, getBatteryStatus()),
            DeviceInfo(R.string.current, getDischargeCurrent(), " mA"),
            DeviceInfo(R.string.cycle_count, getBatteryCycleCount()),
            DeviceInfo(R.string.temperature, getBatteryTemperature(), " °C"),
        )
    }

}