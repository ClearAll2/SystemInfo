package com.lkonlesoft.displayinfo.utils

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import com.lkonlesoft.displayinfo.R
import com.lkonlesoft.displayinfo.helper.dc.DeviceInfo

class SensorUtils(private val context: Context) {

    private val sensorManager by lazy {
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    private val allSensors by lazy { sensorManager.getSensorList(Sensor.TYPE_ALL) }

    fun getSensorList(): List<Sensor> = allSensors

    fun getSensorCount(): Int = allSensors.size

    fun getDashboardData(): List<DeviceInfo> {
        return listOf(
            DeviceInfo(R.string.sensor_count, getSensorCount())
        )
    }

    fun getSensorDetails(sensor: Sensor): List<DeviceInfo> {
        val unit = getSensorMeasureUnit(sensor.type)
        return buildList {
            add(DeviceInfo(R.string.name, sensor.name))
            add(DeviceInfo(R.string.vendor, sensor.vendor))
            add(DeviceInfo(R.string.power, sensor.power, " mA"))
            add(DeviceInfo(R.string.max_range, "%.2f".format(sensor.maximumRange), unit))
        }
    }

    private fun getSensorMeasureUnit(type: Int): String {
        return when (type) {
            Sensor.TYPE_ACCELEROMETER,
            Sensor.TYPE_GRAVITY,
            Sensor.TYPE_LINEAR_ACCELERATION -> " m/s²"

            Sensor.TYPE_MAGNETIC_FIELD,
            Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED -> " µT"

            Sensor.TYPE_GYROSCOPE,
            Sensor.TYPE_GYROSCOPE_UNCALIBRATED -> " rad/s"

            Sensor.TYPE_LIGHT -> " lx"
            Sensor.TYPE_PRESSURE -> " hPa"
            Sensor.TYPE_PROXIMITY -> " cm"
            Sensor.TYPE_AMBIENT_TEMPERATURE -> " °C"
            Sensor.TYPE_RELATIVE_HUMIDITY -> "%"

            else -> ""
        }
    }
}
