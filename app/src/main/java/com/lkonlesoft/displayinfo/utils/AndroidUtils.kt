package com.lkonlesoft.displayinfo.utils

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.lkonlesoft.displayinfo.R
import com.lkonlesoft.displayinfo.helper.getKernelVersion
import java.util.Locale

object AndroidUtils {

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

    fun getGmsVersion(context: Context): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo("com.google.android.gms", 0)
            packageInfo.versionName ?: context.getString(R.string.unknown)
        } catch (_: Exception) {
            context.getString(R.string.n_a)
        }
    }


    fun getAndroidVersion(): String {
        return Build.VERSION.RELEASE ?: "Unknown"
    }

    fun getApiLevel(): Int {
        return Build.VERSION.SDK_INT
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun getSecurityPatch(): String {
        return Build.VERSION.SECURITY_PATCH ?: "Unknown"
    }

    fun getSdkName(): String {
        return try {
            Build.VERSION_CODES::class.java.fields
                .firstOrNull { it.getInt(null) == Build.VERSION.SDK_INT }
                ?.name ?: "Unknown"
        } catch (_: Exception) {
            "Unavailable"
        }
    }

    fun getId(): String {
        return Build.ID ?: "Unknown"
    }

    fun getDisplay(): String {
        return Build.DISPLAY ?: "Unknown"
    }

    fun getIncremental(): String {
        return Build.VERSION.INCREMENTAL ?: "Unknown"
    }

    fun getCodename(): String {
        return Build.VERSION.CODENAME ?: "Unknown"
    }

    fun getType(): String {
        return Build.TYPE ?: "Unknown"
    }

    fun getTags(): String {
        return Build.TAGS ?: "Unknown"
    }

    fun getFingerprint(): String {
        return Build.FINGERPRINT ?: "Unknown"
    }

    fun getHost(): String {
        return Build.HOST ?: "Unknown"
    }

    fun getKernel(): String {
        return getKernelVersion() ?: "Unknown"
    }

    fun getHardware(): String {
        return Build.HARDWARE ?: "Unknown"
    }

    fun getBoard(): String {
        return Build.BOARD ?: "Unknown"
    }

    fun getBootloader(): String {
        return Build.BOOTLOADER ?: "Unknown"
    }
}