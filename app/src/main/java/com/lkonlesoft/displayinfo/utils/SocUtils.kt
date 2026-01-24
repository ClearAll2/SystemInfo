package com.lkonlesoft.displayinfo.utils

import android.app.ActivityManager
import android.content.Context
import android.content.Context.ACTIVITY_SERVICE
import com.lkonlesoft.displayinfo.R
import com.lkonlesoft.displayinfo.helper.dc.DeviceInfo
import java.io.File
import java.io.RandomAccessFile

class SocUtils(private val context: Context) {

    private val CPU_INFO_DIR = "/sys/devices/system/cpu/"

    fun getCpuGovernor(core: Int = 0): String {
        val path = "/sys/devices/system/cpu/cpu$core/cpufreq/scaling_governor"
        return try {
            File(path).readText().trim()
        } catch (_: Exception) {
            context.getString(R.string.unknown)
        }
    }

    fun getAllGovernors(): List<String> {
        val coreCount = Runtime.getRuntime().availableProcessors()
        return (0 until coreCount).map { getCpuGovernor(it) }
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

    fun getGlEsVersion(): String{
        val activityManager: ActivityManager = context.applicationContext.getSystemService(ACTIVITY_SERVICE) as ActivityManager
        return activityManager.deviceConfigurationInfo.glEsVersion
    }

    fun getNumberOfCores(): Int {
        return Runtime.getRuntime().availableProcessors()
    }

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

    fun getCPUInfo(): List<DeviceInfo>{
        val numCores = getNumberOfCores()
        val governor = getCpuGovernor(0)
        val minMaxFrequencies = (0 until numCores).map { getMinMaxFreq(it) }
        val retList = minMaxFrequencies.mapIndexed { index, freq ->
            val minFreq = freq.first
            val maxFreq = freq.second
            DeviceInfo(R.string.core, "${index+1}", if (minFreq != -1L && maxFreq != -1L) "${freq.first} - ${freq.second} MHz" else context.getString(R.string.unknown), 1)
        }
        return listOf(
            DeviceInfo(R.string.cores, numCores.toString()),
            DeviceInfo(R.string.governor, governor)
        ) + retList
    }

    fun getCPUUsage(): List<DeviceInfo>{
        val frequencies = getAllCpuFrequencies()
        val retList = frequencies.mapIndexed { index, freq ->
            DeviceInfo(R.string.core, "${index+1}", if (freq != -1) "$freq MHz" else context.getString(R.string.unknown))
        }
        return retList
    }

}