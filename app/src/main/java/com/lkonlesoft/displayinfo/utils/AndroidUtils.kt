package com.lkonlesoft.displayinfo.utils

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.lkonlesoft.displayinfo.R
import com.lkonlesoft.displayinfo.helper.DeviceInfo
import com.lkonlesoft.displayinfo.helper.getKernelVersion
import java.util.Locale

class AndroidUtils (private val context: Context) {

    fun getPerformanceClass(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Build.VERSION.MEDIA_PERFORMANCE_CLASS
        } else {
            0 // Performance class not available below Android 12
        }
    }

    fun getDeviceLanguage(): String {
        return Locale.getDefault().language // e.g., "en"
    }

    fun getDeviceLocale(): String {
        return Locale.getDefault().toString() // e.g., "en_US"
    }

    fun getGmsVersion(): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo("com.google.android.gms", 0)
            packageInfo.versionName ?: context.getString(R.string.unknown)
        } catch (_: Exception) {
            context.getString(R.string.n_a)
        }
    }

    fun getPlayStoreVersion(): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo("com.android.vending", 0)
            packageInfo.versionName ?: context.getString(R.string.unknown)
        } catch (_: Exception) {
            context.getString(R.string.n_a)
        }
    }


    fun getAndroidVersion(): String {
        return Build.VERSION.RELEASE ?: context.getString(R.string.unknown)
    }

    fun getApiLevel(): Int {
        return Build.VERSION.SDK_INT
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun getSecurityPatch(): String {
        return Build.VERSION.SECURITY_PATCH ?: context.getString(R.string.unknown)
    }

    fun getSdkName(): String {
        return try {
            Build.VERSION_CODES::class.java.fields
                .firstOrNull { it.getInt(null) == Build.VERSION.SDK_INT }
                ?.name ?: context.getString(R.string.unknown)
        } catch (_: Exception) {
            context.getString(R.string.n_a)
        }
    }

    fun getId(): String {
        return Build.ID ?: context.getString(R.string.unknown)
    }

    fun getDisplay(): String {
        return Build.DISPLAY ?: context.getString(R.string.unknown)
    }

    fun getIncremental(): String {
        return Build.VERSION.INCREMENTAL ?: context.getString(R.string.unknown)
    }

    fun getCodename(): String {
        return Build.VERSION.CODENAME ?: context.getString(R.string.unknown)
    }

    fun getType(): String {
        return Build.TYPE ?: context.getString(R.string.unknown)
    }

    fun getTags(): String {
        return Build.TAGS ?: context.getString(R.string.unknown)
    }

    fun getFingerprint(): String {
        return Build.FINGERPRINT ?: context.getString(R.string.unknown)
    }

    fun getHost(): String {
        return Build.HOST ?: context.getString(R.string.unknown)
    }

    fun getKernel(): String {
        return getKernelVersion() ?: context.getString(R.string.unknown)
    }

    fun getHardware(): String {
        return Build.HARDWARE ?: context.getString(R.string.unknown)
    }

    fun getBoard(): String {
        return Build.BOARD ?: context.getString(R.string.unknown)
    }

    fun getBootloader(): String {
        return Build.BOOTLOADER ?: context.getString(R.string.unknown)
    }

    fun getAllData(): List<DeviceInfo> {
        return listOf(
            DeviceInfo(R.string.android_version, getAndroidVersion()),
            DeviceInfo(R.string.api_level, getApiLevel().toString()),
            DeviceInfo(R.string.security_patch,  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) getSecurityPatch() else context.getString(R.string.n_a)),
            DeviceInfo(R.string.sdk, getSdkName()),
            DeviceInfo(R.string.id, getId()),
            DeviceInfo(R.string.build_id, getDisplay()),
            DeviceInfo(R.string.incremental, getIncremental()),
            DeviceInfo(R.string.codename, getCodename()),
            DeviceInfo(R.string.type, getType()),
            DeviceInfo(R.string.tags, getTags()),
            DeviceInfo(R.string.fingerprint, getFingerprint()),
            DeviceInfo(R.string.host, getHost()),
            DeviceInfo(R.string.hardware, getHardware()),
            DeviceInfo(R.string.board, getBoard()),
            DeviceInfo(R.string.bootloader, getBootloader()),
            DeviceInfo(R.string.kernel, getKernel()),
            DeviceInfo(R.string.performance_class, if (getPerformanceClass() > 0) getPerformanceClass().toString() else context.getString(R.string.n_a)),
            DeviceInfo(R.string.google_play_service, getGmsVersion()),
            DeviceInfo(R.string.google_play_store, getPlayStoreVersion()),
            DeviceInfo(R.string.device_language, getDeviceLanguage()),
            DeviceInfo(R.string.device_locale, getDeviceLocale()),
        )
    }

    fun getDashboardData(): List<DeviceInfo> {
        return listOf(
            DeviceInfo(R.string.android_version, getAndroidVersion()),
            DeviceInfo(R.string.api_level, getApiLevel().toString()),
            DeviceInfo(R.string.security_patch,  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) getSecurityPatch() else context.getString(R.string.n_a)),
            DeviceInfo(R.string.sdk, getSdkName()),
            DeviceInfo(R.string.google_play_service, getGmsVersion()),
            DeviceInfo(R.string.google_play_store, getPlayStoreVersion())
        )
    }

}