package com.lkonlesoft.displayinfo.utils

import android.content.Context
import android.content.pm.PackageManager
import com.lkonlesoft.displayinfo.helper.dc.AppInfo
import com.lkonlesoft.displayinfo.helper.drawableToBitmap

class PackageUtils(private val context: Context) {
    private val pm by lazy {
        context.packageManager
    }

    private val packages by lazy {
        pm.getInstalledPackages(PackageManager.GET_META_DATA)
    }

    fun getAllPackages(): List<AppInfo> {
        return packages.map {
            AppInfo(
                name = it.applicationInfo?.loadLabel(pm).toString(),
                packageName = it.packageName,
                icon = drawableToBitmap(it.applicationInfo?.loadIcon(pm)!!),
                versionName = it.versionName
            )
        }
    }
}