package com.lkonlesoft.displayinfo.utils

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.lkonlesoft.displayinfo.R
import com.lkonlesoft.displayinfo.helper.dc.AppInfo
import com.lkonlesoft.displayinfo.helper.dc.DeviceInfo

class PackageUtils(private val context: Context) {
    private val pm by lazy {
        context.packageManager
    }

    private val packages by lazy {
        pm.getInstalledPackages(PackageManager.GET_META_DATA)
    }

    fun getAppCountDetails(): List<DeviceInfo> {
        val appCount = packages.size
        val systemAppCount = packages.count { (it.applicationInfo?.flags ?: 0) and ApplicationInfo.FLAG_SYSTEM != 0 }
        val userAppCount = appCount - systemAppCount
        return listOf(
            DeviceInfo(R.string.all, appCount),
            DeviceInfo(R.string.system, systemAppCount),
            DeviceInfo(R.string.user, userAppCount)
        )
    }

    fun getAllPackages(): List<AppInfo> {
        return packages.map { packageInfo ->
            val isSystemApp = packageInfo.applicationInfo?.flags?.let { flags ->
                (flags and ApplicationInfo.FLAG_SYSTEM) != 0
            } ?: false

            AppInfo(
                name = packageInfo.applicationInfo?.loadLabel(pm).toString(),
                packageName = packageInfo.packageName,
                icon = packageInfo.applicationInfo?.loadIcon(pm),
                versionName = packageInfo.versionName,
                type = if (isSystemApp) 0 else 1,
                status = packageInfo.applicationInfo?.enabled ?: false
            )
        }
    }
}