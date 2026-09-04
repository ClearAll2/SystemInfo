package com.lkonlesoft.displayinfo.utils

import android.app.ActivityManager
import android.app.usage.StorageStatsManager
import android.content.Context
import android.content.Context.ACTIVITY_SERVICE
import android.content.Context.STORAGE_STATS_SERVICE
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.os.storage.StorageManager
import android.text.format.Formatter
import com.lkonlesoft.displayinfo.R
import com.lkonlesoft.displayinfo.helper.dc.DeviceInfo
import com.lkonlesoft.displayinfo.helper.getSystemProperty
import java.io.File

class StorageUtils (private val context: Context) {

    private val am by lazy {
        context.applicationContext.getSystemService(ACTIVITY_SERVICE) as ActivityManager
    }

    private val storageStatsManager by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.getSystemService(STORAGE_STATS_SERVICE) as StorageStatsManager
        } else {
            null
        }
    }

    private val info by lazy {
        ActivityManager.MemoryInfo()
    }

    fun getRAMInfo(): List<DeviceInfo>{
        val totalRAM = getTotalRAM()
        val availableRAM = getAvailableRAM()
        val usedRAM = getUsedRAM()
        val percentageUsed = (usedRAM.toDouble() / totalRAM.toDouble() * 100).toInt()
        val vendor = getRAMVendor()
        return buildList {
            if (!vendor.isNullOrEmpty())
                add(DeviceInfo(R.string.vendor, vendor))
            add(DeviceInfo(R.string.used, percentageUsed, "%", 1))
            add(DeviceInfo(R.string.available_ram, availableRAM, " MB"))
            add(DeviceInfo(R.string.used_ram, usedRAM, " MB"))
            add(DeviceInfo(R.string.total_ram, totalRAM, " MB"))
        }
    }

    fun getInternalStorageInfo(): List<DeviceInfo>{
        val internalStorage = getInternalStorageStats()
        val internalTotal = internalStorage.first
        val internalFree = internalStorage.second
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
        val externalStorage = getExternalStorageStats()
        val externalTotal = externalStorage.first
        if (externalTotal == -1L)
            return emptyList()
        val externalFree = externalStorage.second
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
        am.getMemoryInfo(info)
        return info.totalMem / 1024 / 1024 // MB
    }

    fun getAvailableRAM(): Long {
        am.getMemoryInfo(info)
        return info.availMem / 1024 / 1024 // MB
    }

    fun getUsedRAM(): Long {
        am.getMemoryInfo(info)
        return (info.totalMem - info.availMem) / 1024 / 1024 // MB
    }

    fun getRAMVendor(): String? {
        val properties = listOf(
            "ro.boot.ram_vendor",
            "ro.boot.ddr_vendor",
            "ro.boot.ddr_manuf",
            "ro.vendor.ram.manufacturer",
            "ro.product.ram_vendor",
            "ro.boot.hw.ram_vendor",
            "ro.boot.hardware.ram_vendor"
        )
        for (prop in properties) {
            val value = getSystemProperty(prop)
            if (!value.isNullOrEmpty() && !value.equals(context.getString(R.string.unknown), ignoreCase = true)) {
                return value
            }
        }
        return null
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                storageStatsManager?.let { statsManager ->
                    val total = statsManager.getTotalBytes(StorageManager.UUID_DEFAULT)
                    val free = statsManager.getFreeBytes(StorageManager.UUID_DEFAULT)
                    return Pair(total, free)
                }
            } catch (_: Exception) {
                return Pair(-1, -1)
            }
        }
        return getStorageStats(Environment.getDataDirectory())
    }

    fun formatSize(bytes: Long): String {
        return Formatter.formatShortFileSize(context, bytes)
    }

    fun getExternalStorageStats(): Pair<Long, Long> {
        val externalFiles = context.getExternalFilesDirs(null)
        // Check for secondary storage (usually the second element if present)
        for (i in 1 until externalFiles.size) {
            val file = externalFiles[i]
            if (file != null && Environment.isExternalStorageRemovable(file)) {
                return getStorageStats(file)
            }
        }
        // Fallback for some devices where primary might be removable
        for (file in externalFiles) {
            if (file != null && Environment.isExternalStorageRemovable(file)) {
                return getStorageStats(file)
            }
        }

        return Pair(-1L, -1L)
    }
}