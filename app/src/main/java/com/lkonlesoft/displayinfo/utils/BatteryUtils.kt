package com.lkonlesoft.displayinfo.utils

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import com.lkonlesoft.displayinfo.R
import com.lkonlesoft.displayinfo.helper.dc.DeviceInfo
import kotlin.math.abs
import kotlin.math.max


data class ChargingState(
    val level: Int = -1,
    val plugType: String = "",
    val statusStr: String = "",
    val wattage: Float = 0f,
    val minsTo80: Int = 0,
    val minsTo100: Int = 0,
    val currentMa: Int = -1,
    val tempC: Float = -1f,
    val voltageV: Float = -1f
)

class BatteryUtils (private val context: Context) {

    private val batteryManager by lazy {
        context.applicationContext.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
    }

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
        return healthMap[health] ?: context.getString(R.string.unknown)
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

    fun getChargingWattage(): Float {
        val v = getChargingVoltage()
        val a = abs(getDischargeCurrent()) / 1000f
        return if (v > 0 && a > 0) v * a else 0f
    }

    fun isCharging(): Boolean {
        val intent = getBatteryIntent()
        val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        return status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
    }

    fun getEstimatedChargeTimes(): Pair<Int, Int> {
        val level = getBatteryPercentage()
        if (level !in 0..<100) return Pair(0, 0)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val remainingMs = batteryManager.computeChargeTimeRemaining() ?: -1L
            if (remainingMs > 0) {
                val totalMins = (remainingMs / 60000L).toInt()
                val mins80 = if (level < 80) {
                    ((remainingMs.toDouble() * (80 - level) / (100 - level)) / 60000.0).toInt()
                } else 0
                return Pair(mins80, totalMins)
            }
        }

        // Fallback estimation based on capacity and current
        val capacity = getBatteryCapacity().let { if (it > 0) it else 4000.0 }
        val rawCurrent = abs(getDischargeCurrent())
        val currentMa = if (rawCurrent > 50) rawCurrent.toDouble() else 2000.0

        val remainingMinsTo80 = if (level < 80) {
            val mahTo80 = (80 - level) * capacity / 100.0
            ((mahTo80 / currentMa) * 60.0).toInt()
        } else 0

        val remainingMinsTo100 = run {
            val mahTo80 = if (level < 80) (80 - level) * capacity / 100.0 else 0.0
            val mah80To100 = (100 - max(80, level)) * capacity / 100.0
            val timeMins = ((mahTo80 / currentMa) + (mah80To100 / currentMa) * 1.3) * 60.0
            timeMins.toInt()
        }

        return Pair(max(0, remainingMinsTo80), max(0, remainingMinsTo100))
    }

    fun getChargingState(): ChargingState {
        val intent = getBatteryIntent()

        val level = intent?.let {
            val l = it.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val s = it.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            if (l >= 0 && s > 0) (l * 100 / s) else -1
        } ?: -1

        val plugTypeInt = intent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) ?: -1
        val plugType = plugMap[plugTypeInt] ?: context.getString(R.string.unplugged)

        val statusInt = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val statusStr = statusMap[statusInt] ?: context.getString(R.string.other)

        val temp = intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) ?: -1
        val tempC = if (temp > 0) temp / 10f else -1f

        val rawVoltage = intent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1) ?: -1
        val voltageV = if (rawVoltage > 0) rawVoltage / 1000f else -1f

        val currentMa = getDischargeCurrent()
        val wattage = if (voltageV > 0f && currentMa != -1) voltageV * (abs(currentMa) / 1000f) else 0f
        val (minsTo80, minsTo100) = getEstimatedChargeTimes()

        return ChargingState(
            level = level,
            plugType = plugType,
            statusStr = statusStr,
            wattage = wattage,
            minsTo80 = minsTo80,
            minsTo100 = minsTo100,
            currentMa = currentMa,
            tempC = tempC,
            voltageV = voltageV
        )
    }


    // 🧪 Private helper
    private fun getBatteryIntent(): Intent? {
        val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        return context.registerReceiver(null, intentFilter)
    }

    fun getGeneralData(): List<DeviceInfo> {
        val cycleCount = getBatteryCycleCount()
        val capacity = getBatteryCapacity().toInt()
        return buildList {
            add(DeviceInfo(R.string.battery_level, getBatteryPercentage(), "%"))
            add(DeviceInfo(R.string.health, getBatteryHealth()))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
                add(DeviceInfo(R.string.cycle_count, if (cycleCount >= 0) cycleCount else context.getString(R.string.n_a)))
            if (capacity > 0)
                add(DeviceInfo(R.string.capacity, capacity ,  " mAh"))
            add(DeviceInfo(R.string.technology, getBatteryTechnology()))
        }
    }

    fun getPowerData(): List<DeviceInfo> {
        return buildList {
            add(DeviceInfo(R.string.status, getBatteryStatus()))
            add(DeviceInfo(R.string.temperature, getBatteryTemperature(), " °C"))
            add(DeviceInfo(R.string.current, getDischargeCurrent(), " mA"))
            add(DeviceInfo(R.string.voltage, getChargingVoltage(), " V"))
            add(DeviceInfo(R.string.plug_type, getPlugType()))
        }
    }

    fun getDashboardData(): List<DeviceInfo> {
        val cycleCount = getBatteryCycleCount()
        return buildList {
            add(DeviceInfo(R.string.battery_level, getBatteryPercentage(), "%"))
            add(DeviceInfo(R.string.status, getBatteryStatus()))
            add(DeviceInfo(R.string.health, getBatteryHealth()))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
                add(DeviceInfo(R.string.cycle_count, if (cycleCount >= 0) cycleCount else context.getString(R.string.n_a)))
            add(DeviceInfo(R.string.temperature, getBatteryTemperature(), " °C"))
        }
    }

}