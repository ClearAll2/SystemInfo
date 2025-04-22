package com.lkonlesoft.displayinfo.helper

import android.Manifest
import android.app.ActivityManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.content.pm.PackageManager
import android.media.MediaDrm
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.NetworkCapabilities
import android.opengl.GLES10
import android.os.BatteryManager
import android.os.Build
import android.os.StatFs
import android.telephony.TelephonyManager
import android.util.Base64
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.ui.graphics.Color
import androidx.core.app.ActivityCompat
import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import java.io.RandomAccessFile
import java.text.DecimalFormat
import java.util.UUID

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

fun getUsedMemory(path: File): Long {
    return getTotalMemory(path) - getFreeMemory(path)
}

fun getTotalMemory(path: File): Long{
    if (path.isDirectory && path.exists()){
        val stats = StatFs(path.absolutePath)
        return stats.blockCountLong.times(stats.blockSizeLong)
    }
    return -1
}

fun getFreeMemory(path: File): Long{
    if (path.isDirectory && path.exists()){
        val stats = StatFs(path.absolutePath)
        return stats.availableBlocksLong.times(stats.blockSizeLong)
    }
    return -1
}

fun Long.byteToHuman(): String {
    val symbols = listOf("B", "KB", "MB", "GB", "TB", "PB", "EB")
    var scale = 1L
    symbols.forEach {
        if (this < scale.times(1000L)){
            return String.format("%s %s", DecimalFormat("#.##").format(this.toDouble() / scale), it)
        }
        scale *= 1000L
    }
    return "-1 B"
}

@Suppress("DEPRECATION")
fun getNetworkOldApi(context: Context): String {
    // ConnectionManager instance
    val mConnectivityManager = context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
    val mInfo = mConnectivityManager.activeNetworkInfo

    // If not connected, "-" will be displayed
    if ((mInfo == null) || !mInfo.isConnected) return "-"

    // If Connected to Wifi
    if (mInfo.type == ConnectivityManager.TYPE_WIFI) return "WIFI"

    // If Connected to Mobile
    if (mInfo.type == ConnectivityManager.TYPE_MOBILE) {
        return when (mInfo.subtype) {
            TelephonyManager.NETWORK_TYPE_GPRS, TelephonyManager.NETWORK_TYPE_EDGE, TelephonyManager.NETWORK_TYPE_CDMA, TelephonyManager.NETWORK_TYPE_1xRTT, TelephonyManager.NETWORK_TYPE_IDEN, TelephonyManager.NETWORK_TYPE_GSM -> "2G"
            TelephonyManager.NETWORK_TYPE_UMTS, TelephonyManager.NETWORK_TYPE_EVDO_0, TelephonyManager.NETWORK_TYPE_EVDO_A, TelephonyManager.NETWORK_TYPE_HSDPA, TelephonyManager.NETWORK_TYPE_HSUPA, TelephonyManager.NETWORK_TYPE_HSPA, TelephonyManager.NETWORK_TYPE_EVDO_B, TelephonyManager.NETWORK_TYPE_EHRPD, TelephonyManager.NETWORK_TYPE_HSPAP, TelephonyManager.NETWORK_TYPE_TD_SCDMA -> "3G"
            TelephonyManager.NETWORK_TYPE_LTE, TelephonyManager.NETWORK_TYPE_IWLAN, 19 -> "4G"
            TelephonyManager.NETWORK_TYPE_NR -> "5G"
            else -> "?"
        }
    }
    return "?"
}

@RequiresApi(Build.VERSION_CODES.N)
fun getNetwork(context: Context): String {
    val connectivityManager =
        context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
    val nw = connectivityManager.activeNetwork ?: return "-"
    val actNw = connectivityManager.getNetworkCapabilities(nw) ?: return "-"
    when {
        actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> return "WIFI"
        actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> return "ETHERNET"
        actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
            val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_PHONE_STATE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return "Requires permission"
            }
            when (tm.dataNetworkType) {
                TelephonyManager.NETWORK_TYPE_GPRS,
                TelephonyManager.NETWORK_TYPE_EDGE,
                TelephonyManager.NETWORK_TYPE_CDMA,
                TelephonyManager.NETWORK_TYPE_1xRTT,
                TelephonyManager.NETWORK_TYPE_GSM -> return "2G"
                TelephonyManager.NETWORK_TYPE_UMTS,
                TelephonyManager.NETWORK_TYPE_EVDO_0,
                TelephonyManager.NETWORK_TYPE_EVDO_A,
                TelephonyManager.NETWORK_TYPE_HSDPA,
                TelephonyManager.NETWORK_TYPE_HSUPA,
                TelephonyManager.NETWORK_TYPE_HSPA,
                TelephonyManager.NETWORK_TYPE_EVDO_B,
                TelephonyManager.NETWORK_TYPE_EHRPD,
                TelephonyManager.NETWORK_TYPE_HSPAP,
                TelephonyManager.NETWORK_TYPE_TD_SCDMA -> return "3G"
                TelephonyManager.NETWORK_TYPE_LTE,
                TelephonyManager.NETWORK_TYPE_IWLAN -> return "4G"
                TelephonyManager.NETWORK_TYPE_NR -> return "5G"
                else -> return "?"
            }
        }
        else -> return "?"
    }
}

@RequiresApi(Build.VERSION_CODES.M)
fun getNetInfo(context: Context): NetworkInfo {
    val netInfo = NetworkInfo()
    val connectivityManager = context.getSystemService(CONNECTIVITY_SERVICE)
    if (connectivityManager is ConnectivityManager) {
        val link: LinkProperties =  connectivityManager.getLinkProperties(connectivityManager.activeNetwork) as LinkProperties
        netInfo.ip = link.linkAddresses.joinToString("\n")
        netInfo.domain = link.domains.toString()
        netInfo.interfaces = link.interfaceName.toString()
        netInfo.dnsServer = link.dnsServers.joinToString("\n")
    }
    return netInfo
}

private const val CPU_INFO_DIR = "/sys/devices/system/cpu/"
fun getNumberOfCores(): Int {
    return Runtime.getRuntime().availableProcessors()
}

/**
 * Checking frequencies directories and return current value if exists (otherwise we can
 * assume that core is stopped - value -1)
 */
fun getCurrentFreq(coreNumber: Int): Long {
    val currentFreqPath = "${CPU_INFO_DIR}cpu$coreNumber/cpufreq/scaling_cur_freq"
    return try {
        RandomAccessFile(currentFreqPath, "r").use { it.readLine().toLong() / 1000 }
    } catch (_: Exception) {
        //Timber.e("getCurrentFreq() - cannot read file")
        -1
    }
}


fun getSocRawInfo(): String {
    return File("/proc/cpuinfo").readLines().toString() // Cpu name
}

/**
 * Read max/min frequencies for specific [coreNumber]. Return [Pair] with min and max frequency
 * or [Pair] with -1.
 */
fun getMinMaxFreq(coreNumber: Int): Pair<Long, Long> {
    val minPath = "${CPU_INFO_DIR}cpu$coreNumber/cpufreq/cpuinfo_min_freq"
    val maxPath = "${CPU_INFO_DIR}cpu$coreNumber/cpufreq/cpuinfo_max_freq"
    return try {
        val minMhz = RandomAccessFile(minPath, "r").use { it.readLine().toLong() / 1000 }
        val maxMhz = RandomAccessFile(maxPath, "r").use { it.readLine().toLong() / 1000 }
        Pair(minMhz, maxMhz)
    } catch (_: Exception) {
        //Timber.e("getMinMaxFreq() - cannot read file")
        Pair(-1, -1)
    }
}

fun ActivityManager.getGlEsVersion(): String {
    return deviceConfigurationInfo.glEsVersion
}

fun getWidevineInfo(): Map<String, String> {
    val widevineUUID = UUID.fromString("edef8ba9-79d6-4ace-a3c8-27dcd51d21ed")
    val info = mutableMapOf<String, String>()

    try {
        val mediaDrm = MediaDrm(widevineUUID)

        // Direct string for security level (no constant available)
        val customProps = listOf(
            "vendor",
            "version",
            "securityLevel",
            "algorithms"
        )

        for (prop in customProps) {
            val value = try {
                mediaDrm.getPropertyString(prop)
            } catch (_: Exception) {
                "Unavailable"
            }
            info[prop] = value
        }

        // Special handling for byte array props
        val uniqueId = try {
            val bytes = mediaDrm.getPropertyByteArray("deviceUniqueId")
            Base64.encodeToString(bytes, Base64.NO_WRAP)
        } catch (_: Exception) {
            "Unavailable"
        }

        info["deviceUniqueId"] = uniqueId

        mediaDrm.close()

    } catch (e: Exception) {
        info["error"] = e.message ?: "Error accessing MediaDrm"
    }

    return info
}

fun getClearKeyInfo(): Map<String, String> {
    val clearKeyUUID = UUID.fromString("e2719d58-a985-b3c9-781a-b030af78d30e")
    val info = mutableMapOf<String, String>()
    try {
        val mediaDrm = MediaDrm(clearKeyUUID)

        // Direct string for security level (no constant available)
        val customProps = listOf(
            "vendor",
            "version"
        )

        for (prop in customProps) {
            val value = try {
                mediaDrm.getPropertyString(prop)
            } catch (_: Exception) {
                "Unavailable"
            }
            info[prop] = value
        }

        // Special handling for byte array props
        val uniqueId = try {
            val bytes = mediaDrm.getPropertyByteArray("deviceUniqueId")
            Base64.encodeToString(bytes, Base64.NO_WRAP)
        } catch (_: Exception) {
            "Unavailable"
        }
        mediaDrm.close()

    } catch (e: Exception) {
        info["error"] = e.message ?: "Error accessing MediaDrm"
    }

    return info
}

fun getCpuClockSpeed(core: Int): Int {
    val path = "/sys/devices/system/cpu/cpu$core/cpufreq/scaling_cur_freq"
    return try {
        val file = File(path)
        if (file.exists()) {
            val freqKHz = file.readText().trim().toInt()
            freqKHz / 1000
        } else -1
    } catch (_: Exception) {
        -1
    }
}

fun getAllCpuFrequencies(): List<Int> {
    val cpuCount = Runtime.getRuntime().availableProcessors()
    return (0 until cpuCount).map { getCpuClockSpeed(it) }
}

fun getCpuName(): String {
    return try {
        val file = File("/proc/cpuinfo")
        val lines = file.readLines()
        val processorLine = lines.firstOrNull { it.startsWith("Hardware") || it.startsWith("Processor") || it.startsWith("model name") }
        processorLine?.split(":")?.getOrNull(1)?.trim() ?: "Unknown"
    } catch (_: Exception) {
        "Unavailable"
    }
}

fun getCpuGovernor(core: Int = 0): String {
    val path = "/sys/devices/system/cpu/cpu$core/cpufreq/scaling_governor"
    return try {
        File(path).readText().trim()
    } catch (_: Exception) {
        "Unavailable"
    }
}

fun getAllGovernors(): List<String> {
    val coreCount = Runtime.getRuntime().availableProcessors()
    return (0 until coreCount).map { getCpuGovernor(it) }
}

fun getChargeStatus(info: Int?): String {
    return when (info) {
        BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
        BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
        BatteryManager.BATTERY_STATUS_FULL -> "Full"
        else -> "Unknown"
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
        level <= 50 -> Color(0xFF4CAF50)
        level <= 80 -> Color(0xFFFFC107)
        else -> Color(0xFFF44336)
    }
}

/*fun exportBatteryInfoToJson(context: Context): String {
    val info = mapOf(
        "level" to BatteryUtils.getBatteryPercentage(context),
        "health" to BatteryUtils.getBatteryHealth(context),
        "status" to BatteryUtils.getBatteryStatus(context),
        "plug" to BatteryUtils.getPlugType(context),
        "cycles" to BatteryUtils.getBatteryCycleCount(context),
        "temperature" to BatteryUtils.getBatteryTemperature(context),
        "voltage" to BatteryUtils.getChargingVoltage(),
        "current" to BatteryUtils.getChargingCurrent(),
        "capacity" to BatteryUtils.getBatteryCapacity(context)
    )
    return JSONObject(info).toString(2)
}*/

fun getGpuRenderer(): String {
    return try {
        GLES10.glGetString(GLES10.GL_RENDERER) ?: "Unknown"
    } catch (_: Exception) {
        "Unavailable"
    }
}

fun getGpuVendor(): String {
    return try {
        GLES10.glGetString(GLES10.GL_VENDOR) ?: "Unknown"
    } catch (_: Exception) {
        "Unavailable"
    }
}



