package com.lkonlesoft.displayinfo.utils

import android.app.ActivityManager
import android.content.Context
import android.content.Context.ACTIVITY_SERVICE
import android.os.Environment
import android.os.StatFs
import com.lkonlesoft.displayinfo.R
import com.lkonlesoft.displayinfo.helper.dc.DeviceInfo
import java.io.File

class StorageUtils (private val context: Context) {

    private val am by lazy {
        context.applicationContext.getSystemService(ACTIVITY_SERVICE) as ActivityManager
    }

    private val info by lazy {
        ActivityManager.MemoryInfo()
    }

    fun getRAMInfo(): List<DeviceInfo>{
        val totalRAM = getTotalRAM()
        val availableRAM = getAvailableRAM()
        val usedRAM = totalRAM - availableRAM
        val percentageUsed = (usedRAM.toDouble() / totalRAM.toDouble() * 100).toInt()
        return listOf(
            DeviceInfo(R.string.used, percentageUsed, "%", 1),
            DeviceInfo(R.string.available_ram, availableRAM, " MB"),
            DeviceInfo(R.string.used_ram, usedRAM, " MB"),
            DeviceInfo(R.string.total_ram, totalRAM, " MB")
        )
    }

    fun getInternalStorageInfo(): List<DeviceInfo>{
        val internalTotal = getInternalStorageStats().first
        val internalFree = getInternalStorageStats().second
        val usedInternal = internalTotal - internalFree
        val percentageUsed = (usedInternal.toDouble() / internalTotal.toDouble() * 100).toInt()
        return listOf(
            DeviceInfo(R.string.used, percentageUsed, "%", 1),
            DeviceInfo(R.string.free, internalFree, formatSize(internalFree)),
            DeviceInfo(R.string.used, usedInternal, formatSize(usedInternal)),
            DeviceInfo(R.string.total, internalTotal, formatSize(internalTotal)),
        )
    }

    fun getExternalStorageInfo(): List<DeviceInfo>{
        val externalTotal = getExternalStorageStats().first
        if (externalTotal == -1L)
            return emptyList()
        val externalFree = getExternalStorageStats().second
        val usedExternal = externalTotal - externalFree
        val percentageUsed = (usedExternal.toDouble() / externalTotal.toDouble() * 100).toInt()
        return listOf(
            DeviceInfo(R.string.used, percentageUsed, "%", 1),
            DeviceInfo(R.string.free, externalFree, formatSize(externalFree)),
            DeviceInfo(R.string.used, usedExternal, formatSize(usedExternal)),
            DeviceInfo(R.string.total, externalTotal, formatSize(externalTotal)),
        )
    }


    fun getTotalRAM(): Long {
        val reader = File("/proc/meminfo").bufferedReader().readLine()
        val totalKb = reader.replace(Regex("[^0-9]"), "").toLong()
        return totalKb / 1024 // MB
    }

    fun getAvailableRAM(): Long {
        am.getMemoryInfo(info)
        return info.availMem / 1024 / 1024 // MB
    }

    private fun getStorageStats(path: File): Pair<Long, Long> {
        val stat = StatFs(path.absolutePath)
        val blockSize = stat.blockSizeLong
        val totalBlocks = stat.blockCountLong
        val availableBlocks = stat.availableBlocksLong
        val total = totalBlocks * blockSize
        val free = availableBlocks * blockSize
        return Pair(total, free)
    }

    fun getInternalStorageStats(): Pair<Long, Long> {
        return getStorageStats(Environment.getDataDirectory())
    }

    fun formatSize(bytes: Long): String {
        val kb = bytes / 1024f
        val mb = kb / 1024f
        val gb = mb / 1024f
        return when {
            gb > 1 -> "%.2f GB".format(gb)
            mb > 1 -> "%.2f MB".format(mb)
            else -> "%.2f KB".format(kb)
        }
    }

    fun getExternalStorageStats(): Pair<Long, Long> {
        val externalFiles = context.getExternalFilesDirs(null)
        if (externalFiles.size > 1 && externalFiles[0] != null && externalFiles[1] != null){
            return getStorageStats(externalFiles[1])
        }
        return Pair(-1,-1)
    }
}