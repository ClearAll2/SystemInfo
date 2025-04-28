package com.lkonlesoft.displayinfo.helper

import android.bluetooth.BluetoothProfile
import android.util.Log
import androidx.compose.ui.graphics.Color
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

fun getKernelVersion(): String? {
    return try {
        val p = Runtime.getRuntime().exec("uname -a")
        val inputStream: InputStream? = if (p.waitFor() == 0) {
            p.inputStream
        }
        else {
            p.errorStream
        }
        val br = BufferedReader(InputStreamReader(inputStream))
        val line = br.readLine()
        Log.i("Kernel Version", line)
        br.close()
        line
    }
    catch (ex: Exception) {
        "ERROR: " + ex.message
    }
}

fun connectionStateToString(state: Int): String = when (state) {
    BluetoothProfile.STATE_CONNECTED -> "Connected"
    BluetoothProfile.STATE_CONNECTING -> "Connecting"
    BluetoothProfile.STATE_DISCONNECTED -> "Disconnected"
    BluetoothProfile.STATE_DISCONNECTING -> "Disconnecting"
    else -> "Unknown"
}

fun getStatusColor(value: String): Color {
    return when (value.lowercase()) {
        "good", "full", "charging" -> Color(0xFF4CAF50)
        "dead", "overheat", "failure" -> Color(0xFFF44336)
        else -> Color(0xFFFF9800)
    }
}

fun getTemperatureColor(temp: Float): Color {
    return when {
        temp < 30f -> Color(0xFF4CAF50)
        temp in 30f..40f -> Color(0xFFFFC107)
        else -> Color(0xFFF44336)
    }
}

fun getBatteryLevelColor(level: Long): Color {
    return when {
        level >= 80 -> Color(0xFF4CAF50)
        level >= 30 -> Color(0xFFFFC107)
        else -> Color(0xFFF44336)
    }
}

fun getMemoryLevelColor(level: Long): Color {
    return when {
        level < 40 -> Color(0xFF4CAF50)
        level in 40..80 -> Color(0xFFFFC107)
        else -> Color(0xFFF44336)
    }
}



