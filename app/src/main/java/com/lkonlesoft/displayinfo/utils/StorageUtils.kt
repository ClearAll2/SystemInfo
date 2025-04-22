package com.lkonlesoft.displayinfo.utils

import android.app.ActivityManager
import android.content.Context
import android.content.Context.ACTIVITY_SERVICE
import android.os.Environment
import android.os.StatFs
import java.io.File

object StorageUtils {

    fun getTotalRAM(context: Context): Long {
        val reader = File("/proc/meminfo").bufferedReader().readLine()
        val totalKb = reader.replace(Regex("[^0-9]"), "").toLong()
        return totalKb / 1024 // MB
    }

    fun getAvailableRAM(context: Context): Long {
        val am = context.applicationContext.getSystemService(ACTIVITY_SERVICE) as ActivityManager
        val info = ActivityManager.MemoryInfo()
        am.getMemoryInfo(info)
        return info.availMem / 1024 / 1024 // MB
    }

    fun getUsedRAM(context: Context): Long {
        val total = getTotalRAM(context)
        val free = getAvailableRAM(context)
        return total - free
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

    fun getExternalStorageStats(context: Context): Pair<Long, Long> {
        val externalFiles = context.getExternalFilesDirs(null)
        if (externalFiles.size > 1 && externalFiles[0] != null && externalFiles[1] != null){
            return getStorageStats(externalFiles[1])
        }
        return Pair(-1,-1)
    }

    fun getAppStorageUsage(context: Context): Long {
        return context.filesDir?.let {
            File(it.absolutePath).walkTopDown().map { file -> file.length() }.sum()
        } ?: 0L
    }

    fun getCacheStorageUsage(context: Context): Long {
        return context.cacheDir?.let {
            File(it.absolutePath).walkTopDown().map { file -> file.length() }.sum()
        } ?: 0L
    }


}